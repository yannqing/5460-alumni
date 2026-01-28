// pages/audit/merchant/member/member.js
const app = getApp()

Page({
  data: {
    loading: false,
    memberList: [],
    submitting: false,
    showAddModal: false,
    // 新增成员表单数据
    addForm: {
      wxId: '',
      shopId: '',
      roleOrId: ''
    },
    // 商户选择相关
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',
    // 是否显示商户选择器
    showMerchantSelector: false
  },

  onLoad(options) {
    // 页面加载
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
    // 重置表单数据
    this.setData({
      addForm: {
        wxId: '',
        shopId: '',
        roleOrId: ''
      },
      showAddModal: true
    })
  },

  // 隐藏新增成员弹窗
  hideAddMemberModal() {
    this.setData({
      showAddModal: false
    })
  },

  // 新增成员表单输入处理
  onAddFormInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`addForm.${field}`]: value
    })
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
          title: '请输入校友用户ID',
          icon: 'none'
        })
        return
      }
      
      if (!addForm.roleOrId) {
        wx.showToast({
          title: '请输入角色ID',
          icon: 'none'
        })
        return
      }
      
      this.setData({ submitting: true })
      
      // 调用新增成员接口
      const { post } = require('../../../../utils/request.js')
      const res = await post('/merchant/member/add', {
        merchantId: parseInt(selectedMerchantId),
        wxId: parseInt(addForm.wxId),
        shopId: addForm.shopId ? parseInt(addForm.shopId) : undefined,
        roleOrId: parseInt(addForm.roleOrId)
      })
      
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