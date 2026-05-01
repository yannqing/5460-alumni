let cachedUserLocation = null
let locationPromise = null

function toNumber(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

function isValidCoordinate(lat, lng) {
  return lat !== null && lng !== null && Math.abs(lat) <= 90 && Math.abs(lng) <= 180
}

function formatDistance(meters) {
  if (!Number.isFinite(meters) || meters < 0) return ''
  if (meters < 1000) {
    return `${Math.round(meters)}m`
  }
  return `${(meters / 1000).toFixed(1)}km`
}

function calculateDistanceMeters(lat1, lng1, lat2, lng2) {
  const toRad = (degree) => (degree * Math.PI) / 180
  const earthRadius = 6371000
  const radLat1 = toRad(lat1)
  const radLat2 = toRad(lat2)
  const deltaLat = toRad(lat2 - lat1)
  const deltaLng = toRad(lng2 - lng1)
  const a =
    Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
    Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return earthRadius * c
}

function getUserLocation() {
  if (cachedUserLocation) {
    return Promise.resolve(cachedUserLocation)
  }
  if (locationPromise) {
    return locationPromise
  }
  locationPromise = new Promise((resolve) => {
    wx.getLocation({
      type: 'gcj02',
      success(res) {
        const latitude = toNumber(res.latitude)
        const longitude = toNumber(res.longitude)
        if (isValidCoordinate(latitude, longitude)) {
          cachedUserLocation = { latitude, longitude }
          resolve(cachedUserLocation)
          return
        }
        resolve(null)
      },
      fail() {
        resolve(null)
      },
      complete() {
        locationPromise = null
      }
    })
  })
  return locationPromise
}

Component({
  properties: {
    shop: {
      type: Object,
      value: null,
      observer: 'normalizeShop'
    },
    iconLocation: {
      type: String,
      value: ''
    }
  },

  data: {
    displayName: '',
    displayAddress: '',
    distanceText: '',
    businessHoursText: ''
  },

  methods: {
    normalizeText(value) {
      if (value === null || value === undefined) return ''
      const text = String(value).trim()
      if (!text || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined') {
        return ''
      }
      return text
    },

    normalizeShop(shop) {
      const data = shop || {}
      const name = this.normalizeText(data.shopName) || this.normalizeText(data.name) || '未命名门店'
      const address =
        this.normalizeText(data.address) ||
        [data.province, data.city, data.district]
          .map((item) => this.normalizeText(item))
          .filter(Boolean)
          .join('') ||
        '暂无地址'

      const businessHours =
        this.normalizeText(data.businessHours) ||
        this.normalizeText(data.openingHours) ||
        this.normalizeText(data.openTime)

      this.setData({
        displayName: name,
        displayAddress: address,
        businessHoursText: businessHours ? `营业时间 ${businessHours}` : ''
      })
      this.updateDistance(data)
    },

    async updateDistance(shop) {
      const data = shop || {}
      const latitudeRaw = data.latitude !== undefined && data.latitude !== null ? data.latitude : data.lat
      const longitudeRaw = data.longitude !== undefined && data.longitude !== null ? data.longitude : data.lng
      const shopLatitude = toNumber(latitudeRaw)
      const shopLongitude = toNumber(longitudeRaw)

      if (!isValidCoordinate(shopLatitude, shopLongitude)) {
        this.setData({ distanceText: '' })
        return
      }

      const userLocation = await getUserLocation()
      if (!userLocation) {
        this.setData({ distanceText: '' })
        return
      }

      const meters = calculateDistanceMeters(
        userLocation.latitude,
        userLocation.longitude,
        shopLatitude,
        shopLongitude
      )
      const formattedDistance = formatDistance(meters)
      this.setData({
        distanceText: formattedDistance ? `距您 ${formattedDistance}` : ''
      })
    },

    handleNavigateTap() {
      const shop = this.data.shop || {}
      const latitudeRaw = shop.latitude !== undefined && shop.latitude !== null ? shop.latitude : shop.lat
      const longitudeRaw = shop.longitude !== undefined && shop.longitude !== null ? shop.longitude : shop.lng
      const latitude = toNumber(latitudeRaw)
      const longitude = toNumber(longitudeRaw)

      if (!isValidCoordinate(latitude, longitude)) {
        wx.showToast({
          title: '该门店暂无定位信息',
          icon: 'none'
        })
        return
      }

      wx.openLocation({
        latitude,
        longitude,
        name: this.data.displayName || '门店位置',
        address: this.data.displayAddress || '',
        scale: 18
      })
    },

    handleTap() {
      const shop = this.data.shop || {}
      const id = shop.shopId || shop.id
      this.triggerEvent('cardtap', { id, shop })
    }
  }
})
