// pages/audit/merchant/member/member.js
const config = require('../../../../utils/config.js')
const { alumniApi } = require('../../../../api/api.js')

function debounce(fn, delay) {
  let timer = null
  return function () {
    const context = this
    const args = arguments
    clearTimeout(timer)
    timer = setTimeout(function () {
      fn.apply(context, args)
    }, delay)
  }
}

/**
 * 从校友搜索接口返回的一条记录中取出 wxId（雪花 ID）。
 * - 只用 wxId / wx_id，不要用 id：后者多为 wx_user_info 表自增主键，与 wx_id 不是同一字段。
 * - 若接口把 wxId 以 JSON 数字返回，超过 Number.MAX_SAFE_INTEGER 时已丢精度，不可用。
 */
function pickWxIdFromAlumniRecord(record) {
  if (!record) return ''
  const raw = record.wxId != null ? record.wxId : record.wx_id
  if (raw == null || raw === '') return ''
  if (typeof raw === 'string') {
    const s = raw.trim()
    return /^\d+$/.test(s) ? s : ''
  }
  if (typeof raw === 'number') {
    if (!Number.isSafeInteger(raw)) {
      return ''
    }
    return String(raw)
  }
  return ''
}

Page({
  data: {
    loading: false,
    memberList: [],
    submitting: false,
    showAddModal: false,
    showEditModal: false,
    editSubmitting: false,
    editForm: {
      wxId: '',
      displayName: '',
      shopId: '',
      shopName: '',
      position: ''
    },
    /** 店铺选择器当前服务于「新增」或「编辑」 */
    shopPickerContext: 'add',
    defaultAvatar: config.defaultAvatar,
    // 新增成员表单：name 为搜索/展示，wxId 为选中用户的校友 ID
    addForm: {
      name: '',
      wxId: '',
      shopId: '',
      shopName: '',
      position: ''
    },
    // 新增成员弹窗内：当前商户下的店铺列表（GET /shop/list/{merchantId}）
    addShopList: [],
    shopListLoading: false,
    showShopPicker: false,
    alumniSearchResults: [],
    showAlumniSearchResults: false,
    // 商户选择相关
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',
    // 是否显示商户选择器
    showMerchantSelector: false,
    scrollListHeight: 400,
    deleteSubmitting: false
  },

  onLoad(options) {
    this.searchAlumniDebounced = debounce(this.searchAlumni, 500)
    this.setScrollListHeight()
  },

  preventBubble() {},

  onAddMemberNameInput(e) {
    const value = e.detail.value
    this.setData({
      'addForm.name': value,
      'addForm.wxId': '',
      showAlumniSearchResults: true
    })
    if (value.trim()) {
      this.searchAlumniDebounced(value)
    } else {
      this.setData({ alumniSearchResults: [] })
    }
  },

  onAddMemberNameFocus() {
    if (this.data.addForm.name) {
      this.setData({ showAlumniSearchResults: true })
      if (this.data.alumniSearchResults.length === 0) {
        this.searchAlumni(this.data.addForm.name)
      }
    }
  },

  async searchAlumni(keyword) {
    if (!keyword || !String(keyword).trim()) return
    const k = String(keyword).trim()
    try {
      // POST /users/query/alumni（MySQL），按真实姓名 name 搜索；出参 wxId 为字符串，与库一致
      const res = await alumniApi.queryAlumniListMysql({
        current: 1,
        pageSize: 10,
        name: k
      })
      const list =
        res.data && res.data.code === 200 && res.data.data
          ? res.data.data.records || []
          : []
      const map = new Map()
      list.forEach(item => {
        const wxKey = pickWxIdFromAlumniRecord(item)
        if (wxKey && !map.has(wxKey)) {
          map.set(wxKey, { ...item, wxIdStr: wxKey })
        }
      })
      this.setData({
        alumniSearchResults: Array.from(map.values()).slice(0, 10)
      })
    } catch (e) {
      console.error('搜索用户失败', e)
    }
  },

  selectAddMemberAlumni(e) {
    const ds = e.currentTarget.dataset
    let wxIdStr =
      ds.wxId != null && ds.wxId !== ''
        ? String(ds.wxId).trim()
        : ''
    if (!wxIdStr && ds.index != null) {
      const selected = this.data.alumniSearchResults[ds.index]
      wxIdStr = selected ? pickWxIdFromAlumniRecord(selected) : ''
    }
    if (!wxIdStr || !/^\d+$/.test(wxIdStr)) {
      wx.showToast({
        title: '用户ID异常（精度丢失或非wxId），请重试搜索',
        icon: 'none'
      })
      return
    }
    const selected =
      this.data.alumniSearchResults.find(r => r.wxIdStr === wxIdStr) ||
      this.data.alumniSearchResults.find(r => pickWxIdFromAlumniRecord(r) === wxIdStr)
    const displayName = selected
      ? selected.name || selected.nickname || selected.realName || ''
      : ''
    this.setData({
      'addForm.name': displayName,
      'addForm.wxId': wxIdStr,
      alumniSearchResults: [],
      showAlumniSearchResults: false
    })
  },

  setScrollListHeight() {
    try {
      const res = wx.getSystemInfoSync()
      const navRpx = 190.22
      const navPx = (res.windowWidth * navRpx) / 750
      const contentH = res.windowHeight - navPx
      const scrollH = Math.floor(contentH * 0.5)
      this.setData({ scrollListHeight: scrollH > 200 ? scrollH : 400 })
    } catch (e) {
      this.setData({ scrollListHeight: 400 })
    }
  },

  onShow() {
    // 页面显示时加载商户列表
    this.loadMerchants()
  },

  // 加载商户列表
  async loadMerchants() {
    try {
      this.setData({ loading: true })
      const { get } = require('../../../../utils/request.js')
      const res = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 100 // 加载足够多的商户数据
      })
      
      if (res.data && res.data.code === 200) {
        const merchantList = res.data.data.records || []
        
        // 根据商户列表长度控制是否显示选择器
        const showMerchantSelector = merchantList.length > 1
        
        this.setData({
          merchantList: merchantList,
          showMerchantSelector: showMerchantSelector
        })
        
        // 如果没有选中的商户且列表不为空，自动选择第一个商户
        const { selectedMerchantId } = this.data
        if (!selectedMerchantId && merchantList.length > 0) {
          const firstMerchant = merchantList[0]
          // 将商户ID转换为字符串，避免大数字精度问题
          const merchantIdStr = firstMerchant.merchantId + ''
          this.setData({
            selectedMerchantId: merchantIdStr,
            selectedMerchantName: firstMerchant.merchantName
          })
          // 加载第一个商户的成员列表
          this.loadMemberList(merchantIdStr)
        }
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载成员列表
  async loadMemberList(merchantId = '') {
    try {
      this.setData({ loading: true })
      const { get } = require('../../../../utils/request.js')
      const res = await get(`/merchant/${merchantId}/members`)
      
      if (res.data && res.data.code === 200) {
        this.setData({
          memberList: res.data.data || []
        })
      }
    } catch (error) {
      console.error('加载成员列表失败:', error)
      wx.showToast({
        title: '加载成员列表失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 显示商户选择器
  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },

  // 取消商户选择
  cancelMerchantSelect() {
    this.setData({ showMerchantPicker: false })
  },

  // 选择商户
  selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.merchantId
    const merchantName = e.currentTarget.dataset.merchantName
    
    this.setData({
      selectedMerchantId: merchantId,
      selectedMerchantName: merchantName,
      showMerchantPicker: false
    })
    
    // 根据选中的商户ID加载对应的成员列表
    this.loadMemberList(merchantId)
  },

  // 显示新增成员弹窗
  showAddMemberModal() {
    const mid = this.data.selectedMerchantId
    this.setData({
      addForm: {
        name: '',
        wxId: '',
        shopId: '',
        shopName: '',
        position: ''
      },
      alumniSearchResults: [],
      showAlumniSearchResults: false,
      showShopPicker: false,
      shopPickerContext: 'add',
      showAddModal: true
    })
    if (mid) {
      this.loadShopListForAddModal(mid)
    } else {
      this.setData({ addShopList: [] })
    }
  },

  /** 拉取当前商户下审核通过的店铺列表，供「选择店铺」使用（与店铺管理页一致） */
  async loadShopListForAddModal(merchantId) {
    if (!merchantId) return
    this.setData({ shopListLoading: true, addShopList: [] })
    try {
      const { get } = require('../../../../utils/request.js')
      const res = await get(`/shop/list/${merchantId}`, {
        current: 1,
        size: 100
      })
      if (res.data && res.data.code === 200) {
        const raw = res.data.data
        const records = (raw && raw.records) || raw || []
        this.setData({ addShopList: Array.isArray(records) ? records : [] })
      }
    } catch (e) {
      console.error('加载店铺列表失败', e)
      wx.showToast({ title: '加载店铺列表失败', icon: 'none' })
    } finally {
      this.setData({ shopListLoading: false })
    }
  },

  showShopPickerModal() {
    if (!this.data.selectedMerchantId) {
      wx.showToast({ title: '请先选择商户', icon: 'none' })
      return
    }
    if (this.data.shopListLoading) {
      wx.showToast({ title: '店铺列表加载中…', icon: 'none' })
      return
    }
    this.setData({ showShopPicker: true, shopPickerContext: 'add' })
  },

  showEditShopPickerModal() {
    if (!this.data.selectedMerchantId) {
      wx.showToast({ title: '请先选择商户', icon: 'none' })
      return
    }
    if (this.data.shopListLoading) {
      wx.showToast({ title: '店铺列表加载中…', icon: 'none' })
      return
    }
    this.setData({ showShopPicker: true, shopPickerContext: 'edit' })
  },

  cancelShopSelect() {
    this.setData({ showShopPicker: false })
  },

  selectShopForPicker(e) {
    const shopId = e.currentTarget.dataset.shopId
    const shopName = e.currentTarget.dataset.shopName
    const ctx = this.data.shopPickerContext || 'add'
    const sid = shopId != null && shopId !== '' ? String(shopId) : ''
    const name = shopName || ''
    if (ctx === 'edit') {
      this.setData({
        'editForm.shopId': sid,
        'editForm.shopName': name,
        showShopPicker: false
      })
    } else {
      this.setData({
        'addForm.shopId': sid,
        'addForm.shopName': name,
        showShopPicker: false
      })
    }
  },

  // 隐藏新增成员弹窗
  hideAddMemberModal() {
    this.setData({
      showAddModal: false,
      showShopPicker: false
    })
  },

  confirmDeleteMember(e) {
    const wxIdStr =
      e.currentTarget.dataset.wxId != null ? String(e.currentTarget.dataset.wxId).trim() : ''
    if (!/^\d+$/.test(wxIdStr)) {
      wx.showToast({ title: '成员ID无效', icon: 'none' })
      return
    }
    if (!this.data.selectedMerchantId) {
      wx.showToast({ title: '请先选择商户', icon: 'none' })
      return
    }
    if (this.data.deleteSubmitting) return
    wx.showModal({
      title: '确认删除',
      content: '确定将该成员从当前商户中移除吗？',
      confirmColor: '#ff4d4f',
      success: res => {
        if (res.confirm) {
          this.deleteMember(wxIdStr)
        }
      }
    })
  },

  async deleteMember(wxIdStr) {
    const { selectedMerchantId } = this.data
    const merchantIdStr = String(selectedMerchantId || '').trim()
    if (!merchantIdStr) {
      wx.showToast({ title: '请先选择商户', icon: 'none' })
      return
    }
    this.setData({ deleteSubmitting: true })
    try {
      const { post } = require('../../../../utils/request.js')
      const res = await post('/merchant/member/delete', {
        merchantId: merchantIdStr,
        wxId: wxIdStr
      })
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '已删除', icon: 'success' })
        this.loadMemberList(selectedMerchantId)
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '删除失败',
          icon: 'none'
        })
      }
    } catch (err) {
      console.error('删除成员失败', err)
      wx.showToast({ title: '删除失败', icon: 'none' })
    } finally {
      this.setData({ deleteSubmitting: false })
    }
  },

  openEditMember(e) {
    const wxIdStr =
      e.currentTarget.dataset.wxId != null ? String(e.currentTarget.dataset.wxId).trim() : ''
    if (!/^\d+$/.test(wxIdStr)) {
      wx.showToast({ title: '成员ID无效', icon: 'none' })
      return
    }
    const selected = this.data.memberList.find(m => String(m.wxId) === wxIdStr)
    if (!selected) {
      wx.showToast({ title: '未找到该成员', icon: 'none' })
      return
    }
    const displayName = selected.nickname || selected.name || '成员'
    const shopIdStr =
      selected.shopId != null && selected.shopId !== ''
        ? String(selected.shopId)
        : ''
    const shopName = selected.shopName || ''
    this.setData({
      showEditModal: true,
      shopPickerContext: 'edit',
      editForm: {
        wxId: wxIdStr,
        displayName,
        shopId: shopIdStr,
        shopName,
        position: selected.position != null ? String(selected.position) : ''
      }
    })
    const mid = this.data.selectedMerchantId
    if (mid) {
      this.loadShopListForAddModal(mid)
    }
  },

  hideEditMemberModal() {
    this.setData({ showEditModal: false, showShopPicker: false })
  },

  /** 原生 input 上 data-* 在 bindinput 里往往取不到 dataset，勿用通用 data-field 写法 */
  onEditPositionInput(e) {
    this.setData({ 'editForm.position': e.detail.value })
  },

  async submitEditMember() {
    try {
      const { selectedMerchantId, editForm } = this.data
      if (!selectedMerchantId) {
        wx.showToast({ title: '请先选择商户', icon: 'none' })
        return
      }
      const pos = editForm.position != null ? String(editForm.position).trim() : ''
      if (!pos) {
        wx.showToast({ title: '请输入职务', icon: 'none' })
        return
      }
      const wxIdStr = String(editForm.wxId || '').trim()
      if (!/^\d+$/.test(wxIdStr)) {
        wx.showToast({ title: '成员ID无效', icon: 'none' })
        return
      }

      this.setData({ editSubmitting: true })
      const { post } = require('../../../../utils/request.js')
      const payload = {
        merchantId: String(selectedMerchantId).trim(),
        wxId: wxIdStr,
        position: pos
      }
      const shopIdStr = String(editForm.shopId || '').trim()
      if (shopIdStr && /^\d+$/.test(shopIdStr)) {
        payload.shopId = shopIdStr
      }
      const res = await post('/merchant/member/update-role', payload)
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        this.hideEditMemberModal()
        this.loadMemberList(selectedMerchantId)
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '保存失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('更新成员失败', error)
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      this.setData({ editSubmitting: false })
    }
  },

  onAddPositionInput(e) {
    this.setData({ 'addForm.position': e.detail.value })
  },

  // 提交新增成员
  async submitAddMember() {
    try {
      const { selectedMerchantId, addForm } = this.data
      
      // 表单验证
      if (!selectedMerchantId) {
        wx.showToast({
          title: '请先选择商户',
          icon: 'none'
        })
        return
      }
      
      if (!addForm.wxId) {
        wx.showToast({
          title: '请搜索并选择用户',
          icon: 'none'
        })
        return
      }
      
      if (!addForm.position || !String(addForm.position).trim()) {
        wx.showToast({
          title: '请输入职务',
          icon: 'none'
        })
        return
      }

      const wxIdStr = String(addForm.wxId || '').trim()
      if (!/^\d+$/.test(wxIdStr)) {
        wx.showToast({ title: '用户 ID 无效，请重新选择', icon: 'none' })
        return
      }

      this.setData({ submitting: true })

      // 调用新增成员接口（merchantId / wxId / shopId 均为雪花 ID，必须以字符串传递，避免 JS Number 精度丢失）
      const { post } = require('../../../../utils/request.js')
      const merchantIdStr = String(selectedMerchantId).trim()
      const shopIdStr = String(addForm.shopId || '').trim()
      const payload = {
        merchantId: merchantIdStr,
        wxId: wxIdStr,
        position: String(addForm.position).trim()
      }
      if (shopIdStr && /^\d+$/.test(shopIdStr)) {
        payload.shopId = shopIdStr
      }

      const res = await post('/merchant/member/add', payload)
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '新增成员成功',
          icon: 'success'
        })
        
        // 隐藏弹窗
        this.hideAddMemberModal()
        
        // 重新加载成员列表
        this.loadMemberList(selectedMerchantId)
      } else {
        wx.showToast({
          title: res.data.msg || '新增成员失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('新增成员失败:', error)
      wx.showToast({
        title: '新增成员失败',
        icon: 'none'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})