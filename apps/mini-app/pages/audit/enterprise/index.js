// pages/audit/enterprise/index.js
const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    enterpriseList: [],
    loading: false,
    // 分页相关
    current: 1,
    pageSize: 10,
    total: 0,
    hasMore: true,
    // 筛选相关
    sortField: 'createTime',
    sortOrder: 'ascend',
    placeName: '',
    applicationStatus: null,
    placeType: 1, // 1-企业
    applicantName: '',
    // 校友会选择相关
    alumniAssociationList: [],
    selectedAlumniAssociationId: null,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    hasSingleAlumniAssociation: false,
    hasAlumniAdminPermission: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.initPage();
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.setData({ current: 1, hasMore: true });
    this.loadEnterpriseList();
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ current: this.data.current + 1 });
      this.loadEnterpriseList(true);
    }
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  /**
   * 初始化页面
   */
  async initPage() {
    await this.loadAlumniAssociationList();
  },

  /**
   * 加载校友会列表
   */
  async loadAlumniAssociationList() {
    try {
      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || [];
      
      // 查找所有校友会管理员角色
      const alumniAdminRoles = roles.filter(role => 
        role.roleName === '校友会管理员' || role.remark === '校友会管理员'
      );
      
      // 设置是否有校友会管理员身份
      this.setData({
        hasAlumniAdminPermission: alumniAdminRoles.length > 0
      });
      
      if (alumniAdminRoles.length > 0) {
        // 存储所有校友会数据
        const alumniAssociationList = [];
        
        // 遍历所有校友会管理员角色，创建校友会数据
        for (const alumniAdminRole of alumniAdminRoles) {
          // 尝试从不同可能的位置获取ID
          let alumniAssociationId = null;
          
          // 检查直接字段
          if (alumniAdminRole.alumniAssociationId) {
            alumniAssociationId = alumniAdminRole.alumniAssociationId;
          }
          // 检查嵌套的organization字段
          else if (alumniAdminRole.organization && alumniAdminRole.organization.alumniAssociationId) {
            alumniAssociationId = alumniAdminRole.organization.alumniAssociationId;
          }
          // 检查organizeId字段（作为备用）
          else if (alumniAdminRole.organizeId) {
            alumniAssociationId = alumniAdminRole.organizeId;
          }
          // 检查嵌套的organization.organizeId字段
          else if (alumniAdminRole.organization && alumniAdminRole.organization.organizeId) {
            alumniAssociationId = alumniAdminRole.organization.organizeId;
          }
          
          if (alumniAssociationId) {
            // 获取协会名称
            let associationName = alumniAdminRole.associationName || (alumniAdminRole.organization && alumniAdminRole.organization.associationName) || '校友会';
            
            // 创建基本的校友会对象
            const basicAlumniData = {
              id: alumniAssociationId,
              alumniAssociationId: alumniAssociationId,
              alumniAssociationName: `${associationName} (ID: ${alumniAssociationId})`
            };
            
            // 检查是否已经存在相同ID的校友会
            const existingIndex = alumniAssociationList.findIndex(item => 
              item.alumniAssociationId === alumniAssociationId
            );
            
            // 如果不存在，则添加到列表
            if (existingIndex === -1) {
              alumniAssociationList.push(basicAlumniData);
            }
          }
        }
        
        // 设置校友会列表
        this.setData({
          alumniAssociationList: alumniAssociationList
        });
        
        // 处理校友会选择逻辑
        this.handleAlumniAssociationSelection(alumniAssociationList);
      } else {
        // 没有找到校友会管理员角色
        this.setData({
          alumniAssociationList: []
        });
      }
    } catch (error) {
      console.error('加载校友会列表失败:', error);
      // 发生错误时，设置为空数组
      this.setData({
        alumniAssociationList: []
      });
    }
  },

  /**
   * 处理校友会选择逻辑
   */
  async handleAlumniAssociationSelection(alumniAssociationList) {
    if (alumniAssociationList.length === 1) {
      // 只有一个校友会权限，自动选择并禁用选择器
      const singleAlumni = alumniAssociationList[0];
      this.setData({
        selectedAlumniAssociationId: singleAlumni.alumniAssociationId,
        selectedAlumniAssociationName: singleAlumni.alumniAssociationName,
        hasSingleAlumniAssociation: true
      });
      // 加载企业列表
      await this.loadEnterpriseList();
    } else if (alumniAssociationList.length > 1) {
      // 多个校友会权限，正常显示选择器
      this.setData({
        hasSingleAlumniAssociation: false
      });
    } else {
      // 没有校友会权限
      this.setData({
        hasSingleAlumniAssociation: false
      });
      // 加载企业列表
      await this.loadEnterpriseList();
    }
  },

  /**
   * 显示校友会选择器
   */
  showAlumniAssociationSelector() {
    this.setData({ showAlumniAssociationPicker: true });
  },

  /**
   * 选择校友会
   */
  async selectAlumniAssociation(e) {
    const alumniAssociationId = e.currentTarget.dataset.alumniAssociationId;
    const alumniAssociationName = e.currentTarget.dataset.alumniAssociationName;
    
    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false,
      current: 1, // 重置页码
      hasMore: true // 重置是否有更多数据
    });
    
    // 加载企业列表
    await this.loadEnterpriseList();
  },

  /**
   * 取消选择校友会
   */
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false });
  },

  /**
   * 加载企业列表
   */
  loadEnterpriseList(loadMore = false) {
    this.setData({ loading: true });
    
    const { current, pageSize, sortField, sortOrder, placeName, applicationStatus, placeType, selectedAlumniAssociationId, applicantName } = this.data;
    
    // 构建请求参数
    const params = {
      current: current,
      pageSize: pageSize,
      sortField: sortField,
      sortOrder: sortOrder,
      placeType: placeType
    };
    
    // 添加可选参数
    if (placeName) params.placeName = placeName;
    if (applicationStatus !== null && applicationStatus !== undefined) params.applicationStatus = applicationStatus;
    if (selectedAlumniAssociationId) params.alumniAssociationId = selectedAlumniAssociationId;
    if (applicantName) params.applicantName = applicantName;
    
    // 发送POST请求
    const token = wx.getStorageSync('token') || (wx.getStorageSync('userInfo') || {}).token || '';
    
    wx.request({
      url: `${app.globalData.baseUrl}/alumni-place/management/application/page`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        ...(token ? { token, 'x-token': token } : {})
      },
      data: params,
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const newData = res.data.data.records || [];
          const total = res.data.data.total || 0;
          
          this.setData({
            enterpriseList: loadMore ? [...this.data.enterpriseList, ...newData] : newData,
            total: total,
            hasMore: (loadMore ? this.data.enterpriseList.length : 0) + newData.length < total,
            loading: false
          });
        } else {
          console.error('获取企业列表失败:', res.data && res.data.msg || '接口调用失败');
          this.setData({
            enterpriseList: loadMore ? this.data.enterpriseList : [],
            loading: false
          });
        }
      },
      fail: (error) => {
        console.error('获取企业列表异常:', error);
        this.setData({
          enterpriseList: loadMore ? this.data.enterpriseList : [],
          loading: false
        });
      },
      complete: () => {
        wx.stopPullDownRefresh();
      }
    });
  },

  /**
   * 查看详情 - 已注释
   */
  /*
  viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/audit/enterprise/detail/detail?id=${id}`
    });
  }
  */

  /**
   * 审核通过企业
   */
  approveEnterprise(e) {
    const applicationId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '审核通过',
      content: '确定要审核通过该企业吗？',
      success: (res) => {
        if (res.confirm) {
          this.submitAudit(applicationId, 1, '');
        }
      }
    });
  },

  /**
   * 审核拒绝企业
   */
  rejectEnterprise(e) {
    const applicationId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '审核拒绝',
      content: '请输入拒绝原因',
      editable: true,
      placeholderText: '请输入审核备注',
      success: (res) => {
        if (res.confirm && res.content) {
          this.submitAudit(applicationId, 2, res.content);
        } else if (res.confirm) {
          wx.showToast({
            title: '请输入拒绝原因',
            icon: 'none'
          });
        }
      }
    });
  },

  /**
   * 提交审核
   */
  submitAudit(applicationId, applicationStatus, reviewRemark) {
    const token = wx.getStorageSync('token') || (wx.getStorageSync('userInfo') || {}).token || '';
    
    // Build request data with required parameters
    const requestData = {
      applicationId: applicationId,
      applicationStatus: applicationStatus
    };
    
    // Add reviewRemark if provided (required for rejection)
    if (reviewRemark) {
      requestData.reviewRemark = reviewRemark;
    }
    
    // Use the exact endpoint provided
    wx.request({
      url: `${app.globalData.baseUrl}/alumni-place/management/application/approve`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        ...(token ? { token, 'x-token': token } : {})
      },
      data: requestData,
      success: (res) => {
        if (res.data && res.data.code === 200) {
          wx.showToast({
            title: applicationStatus === 1 ? '审核通过成功' : '审核拒绝成功',
            icon: 'success'
          });
          
          // 重新加载企业列表
          this.setData({ current: 1, hasMore: true });
          this.loadEnterpriseList();
        } else {
          wx.showToast({
            title: res.data && res.data.msg || '审核失败',
            icon: 'none'
          });
        }
      },
      fail: (error) => {
        console.error('审核异常:', error);
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        });
      }
    });
  }
})