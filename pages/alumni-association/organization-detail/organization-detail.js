// pages/alumni-association/organization-detail/organization-detail.js
const { post } = require('../../../utils/request.js')

Page({
  data: {
    roleList: [], // 存储角色列表
    loading: false // 加载状态
  },

  onLoad(options) {
    // 从options中获取校友会ID，支持两种参数名：associationId和id
    this.alumniAssociationId = options.associationId || options.id
    
    if (this.alumniAssociationId) {
      this.initPage()
    } else {
      wx.showToast({
        title: '缺少校友会ID参数',
        icon: 'none'
      })
      // 可以选择返回上一页或跳转到其他页面
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  // 初始化页面数据
  initPage() {
    this.loadOrganizationTree()
  },
  
  // 加载组织架构树
  loadOrganizationTree() {
    this.setData({
      loading: true
    })
    
    // 调用API获取组织架构树
    post('/AlumniAssociation/organizationTree', {
      alumniAssociationId: this.alumniAssociationId
    }).then(res => {
      if (res.data && res.data.code === 200) {
        // 添加展开状态
        const dataWithExpandedState = this.addExpandedState(res.data.data || [])
        
        this.setData({
          roleList: dataWithExpandedState
        })
      } else {
        wx.showToast({
          title: res.data.msg || '加载失败',
          icon: 'none'
        })
      }
    }).catch(err => {
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
      console.error('加载组织架构树失败:', err)
    }).finally(() => {
      this.setData({
        loading: false
      })
    })
  },
  
  // 添加展开状态
  addExpandedState(data) {
    return data.map(item => {
      return {
        ...item,
        expanded: false, // 默认折叠
        children: item.children ? this.addExpandedState(item.children) : []
      }
    })
  },
  
  // 切换展开状态
  toggleExpand(e) {
    const { id } = e.currentTarget.dataset
    const newRoleList = this.toggleExpandRecursive([...this.data.roleList], id)
    this.setData({
      roleList: newRoleList
    })
  },
  
  // 递归切换展开状态
  toggleExpandRecursive(list, id) {
    return list.map(item => {
      if (item.roleOrId === id) {
        return {
          ...item,
          expanded: !item.expanded
        }
      } else if (item.children && item.children.length > 0) {
        return {
          ...item,
          children: this.toggleExpandRecursive(item.children, id)
        }
      }
      return item
    })
  }
})