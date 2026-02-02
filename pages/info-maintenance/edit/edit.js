// pages/info-maintenance/edit/edit.js
const { get, put } = require('../../../utils/request.js')

Page({
  data: {
    formData: {
      associationName: '',
      contactInfo: '',
      location: '',
      logo: '',
      bgImg: '',
      status: 1
    },
    submitting: false
  },
  onLoad(options) {
    this.alumniAssociationId = options.alumniAssociationId
    this.loadAlumniDetail()
  },
  
  async loadAlumniDetail() {
    try {
      const res = await get(`/alumniAssociationManagement/detail/${this.alumniAssociationId}`)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        this.setData({
          formData: {
            associationName: data.associationName || '',
            contactInfo: data.contactInfo || '',
            location: data.location || '',
            logo: data.logo || '',
            bgImg: data.bgImg || '',
            status: data.status || 1
          }
        })
      }
    } catch (error) {
      console.error('Failed to load alumni detail:', error)
    }
  },
  
  handleInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    
    this.setData({
      [`formData.${field}`]: value
    })
  },
  
  handleStatusChange(e) {
    this.setData({
      'formData.status': parseInt(e.detail.value)
    })
  },
  
  async submitForm() {
    this.setData({ submitting: true })
    
    try {
      const formData = this.data.formData
      const payload = {
        alumniAssociationId: this.alumniAssociationId,
        associationName: formData.associationName,
        contactInfo: formData.contactInfo,
        location: formData.location,
        logo: formData.logo,
        bgImg: formData.bgImg,
        status: formData.status
      }
      
      const res = await put('/alumniAssociationManagement/update', payload)
      
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '更新成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '更新失败', icon: 'none' })
      }
    } catch (error) {
      console.error('Submit error:', error)
      wx.showToast({ title: '网络错误', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
