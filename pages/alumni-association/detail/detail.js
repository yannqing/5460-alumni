// pages/alumni-association/detail/detail.js
const { associationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

const DEFAULT_ALUMNI_AVATAR = config.defaultAlumniAvatar
const DEFAULT_COVER = config.defaultCover

Page({
  data: {
    // 图标路径
    iconSchool: config.getIconUrl('xx.png'),
    iconLocation: config.getIconUrl('position.png'),
    associationId: '',
    associationInfo: null,
    activeTab: 0,
    tabs: ['基本信息', '成员列表', '组织结构'],//, '关系图谱'],
    members: [],
    // 图谱数据（预留后端接口）
    graphData: null,
    canvasReady: false,
    activities: [],
    activityList: [],
    enterpriseList: [],
    notifications: [],
    benefitActivities: [],
    nearbyBenefits: [],
    alumniEnterprises: [],
    alumniShops: [],
    loading: false,
    // 测试用：校友总会 ID（后续可由接口返回 unionId 替换）
    testUnionId: 1,
    // 加入校友会申请表单
    showJoinModal: false,
    joinForm: {
      realName: '',
      graduationYear: '',
      major: '',
      remark: ''
    },
    joinSubmitting: false,

    // 统计卡片
    selectedNode: null,
    showNodeCard: false,

    // 组织结构数据
    roleList: [], // 存储角色列表
    organizationLoading: false // 组织结构加载状态
  },

  async onLoad(options) {
    this.setData({ associationId: options.id })
    // 确保已登录后再加载数据
    await this.ensureLogin()
    this.loadAssociationDetail()
  },

  onShow() {
    // 页面显示时重新检查登录状态并刷新数据
    this.ensureLogin().then(() => {
      // 重新加载详情数据以获取最新的申请状态
      if (this.data.associationId) {
        this.loadAssociationDetail()
      }
    })
  },

  // 确保已登录
  async ensureLogin() {
    const app = getApp()
    const isLogin = app.checkHasLogined()

    if (!isLogin) {
      try {
        await app.initApp()
      } catch (error) {
        wx.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
        throw error
      }
    }
  },

  // 格式化时间为 月-日 时:分
  formatTime(dateString) {
    if (!dateString) return ''
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return ''
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
  },

  // 加载校友会详情
  async loadAssociationDetail() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const res = await associationApi.getAssociationDetail(this.data.associationId)

      if (res.data && res.data.code === 200) {
        const item = res.data.data || {}

        // 数据映射（与后端字段保持同步）
        // 后端字段示例（AlumniAssociationListVo）：
        // 实际返回结构中，ID 信息分散在不同对象中：
        // - 校友会主键：alumni_association_id（后端未在 VO 中显式暴露，前端使用请求时的 id）
        // - 母校 ID：data.schoolInfo.schoolId
        // - 校促会 ID：目前 VO 中未暴露，前端暂不直接使用
        const schoolInfo = item.schoolInfo || {}
        const platformInfo = item.platform || {}

        const mappedInfo = {
          // 使用前端当前请求的 id 作为校友会 ID，避免依赖后端未暴露字段
          id: this.data.associationId,
          associationId: this.data.associationId,
          name: item.associationName,
          associationName: item.associationName,
          // 从 schoolInfo 中读取真正的母校 ID，避免为 null/undefined
          schoolId: schoolInfo.schoolId || null,
          // 平台 ID 后端当前未直接返回，这里预留字段，兼容后续扩展
          platformId: platformInfo.platformId || platformInfo.id || null,
          presidentUserId: item.presidentUserId,
          // 优先使用后端返回的 logo 字段，如果没有则使用默认头像，与列表页保持一致
          icon: item.logo ? config.getImageUrl(item.logo) : '/assets/avatar/avatar-2.png',
          cover: DEFAULT_COVER, // 后端暂无封面字段，使用默认
          location: item.location || '',
          memberCount: item.memberCount || 0,
          contactInfo: item.contactInfo || '',
          // 预留字段（后端暂无，使用默认值）
          schoolName: schoolInfo.schoolName || '', // 优先使用返回的学校名称
          address: item.location || '', // 使用 location 作为地址
          isJoined: false, // 后端暂无此字段
          isCertified: false, // 后端暂无此字段
          president: '', // 需要根据 presidentUserId 查询，或后端返回
          vicePresidents: [], // 后端暂无此字段
          establishedYear: null, // 后端暂无此字段
          description: '', // 后端暂无此字段
          certifications: [], // 后端暂无此字段
          // 申请状态：0-待审核, 1-已通过, 2-已拒绝, 3-已撤销, null-未申请
          applicationStatus: item.applicationStatus !== undefined ? item.applicationStatus : null
        }

        // 处理活动列表，格式化时间
        const formattedActivityList = (item.activityList || []).map(activity => ({
          ...activity,
          startTime: this.formatTime(activity.startTime)
        }))

        this.setData({
          associationInfo: mappedInfo,
          activityList: formattedActivityList,
          enterpriseList: item.enterpriseList || [],
          loading: false
        })

        // 加载其他相关数据（成员、活动等，这些接口可能还未实现）
        // this.loadActivities()
        // this.loadNotifications()
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 加载成员列表
  async loadMembers() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const res = await associationApi.getMemberPage({
        alumniAssociationId: this.data.associationId,
        page: 1,
        size: 20
      })

      if (res.data && res.data.code === 200) {
        const memberData = res.data.data || {}
        const records = memberData.records || []

        // 根据职务排序：pid 为 null 的排在最前面（如会长）
        records.sort((a, b) => {
          const aPid = a.organizeArchiRole ? a.organizeArchiRole.pid : undefined
          const bPid = b.organizeArchiRole ? b.organizeArchiRole.pid : undefined

          if (aPid === null && bPid !== null) return -1
          if (aPid !== null && bPid === null) return 1
          return 0
        })

        // 数据映射
        const mappedMembers = records.map(item => {
          // 处理头像URL
          let avatarUrl = item.avatarUrl || item.avatar || ''
          if (avatarUrl) {
            avatarUrl = config.getImageUrl(avatarUrl)
          } else {
            avatarUrl = config.defaultAvatar
          }

          // 获取组织架构角色信息
          const organizeArchiRole = item.organizeArchiRole || {}
          const roleOrName = organizeArchiRole.roleOrName || '成员'
          // 获取signature字段（与avatarUrl同级）
          const signature = item.signature || '暂无公司信息'

          return {
            id: item.wxId,
            wxId: item.wxId,
            avatar: avatarUrl,
            nickname: item.nickname || '',
            name: item.name || item.realName || '未知用户',
            role: roleOrName,
            company: signature
          }
        })

        this.setData({
          members: mappedMembers,
          loading: false
        })
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载成员列表失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载成员列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载成员列表失败，请重试',
        icon: 'none'
      })
    }
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ activeTab: index })

    // 切换到成员列表标签时
    if (index === 1) {
      // 如果还没加载过成员数据，则加载
      if (this.data.members.length === 0) {
        this.loadMembers()
      }
    }
    // 切换到组织结构标签时，加载组织结构数据
    else if (index === 2) {
      // 如果还没加载过组织结构数据，则加载
      if (this.data.roleList.length === 0) {
        this.loadOrganizationTree()
      }
    }
    /* // 切换到图谱标签时
    else if (index === 3) {
      // 如果还没加载过数据，则加载
      if (!this.data.graphData) {
        this.loadGraphData()
      }
      // 每次切换到图谱页面都重新初始化 Canvas
      setTimeout(() => {
        // 先停止之前的动画
        if (this.graphContext) {
          this.graphContext.stopAnimation()
        }
        // 重新初始化
        this.initGraph()
      }, 100)
    } else {
      // 切换离开图谱页面时，停止动画节省性能
      if (this.graphContext) {
        this.graphContext.stopAnimation()
      }
    } */
  },

  viewMemberDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id) {
      wx.navigateTo({
        url: `/pages/alumni/detail/detail?id=${id}`
      })
    }
  },

  // 加载图谱数据（预留后端接口）
  async loadGraphData() {
    // TODO: 对接后端接口获取图谱数据
    // const res = await associationApi.getRelationGraph(this.data.associationId)

    // 模拟数据
    const mockGraphData = {
      nodes: [
        // 核心节点
        { id: "00后", group: 1, val: 50 },
        { id: "广东", group: 1, val: 40 },
        { id: "程序员", group: 1, val: 45 },
        // 兴趣节点
        { id: "游戏", group: 2, val: 30 },
        { id: "搞钱", group: 2, val: 35 },
        { id: "撸猫", group: 2, val: 25 },
        { id: "夜宵", group: 2, val: 20 },
        { id: "数码", group: 2, val: 28 },
        { id: "二次元", group: 2, val: 32 },
        // 长尾节点
        { id: "原神", group: 3, val: 15 },
        { id: "王者", group: 3, val: 15 },
        { id: "基金", group: 3, val: 18 },
        { id: "副业", group: 3, val: 20 },
        { id: "脱发", group: 3, val: 10 },
        { id: "咖啡", group: 3, val: 12 },
        { id: "键盘", group: 3, val: 14 },
        { id: "显卡", group: 3, val: 16 },
        { id: "早茶", group: 3, val: 15 },
        { id: "加班", group: 3, val: 12 },
        { id: "番剧", group: 3, val: 18 },
        { id: "Coser", group: 3, val: 10 },
        { id: "Switch", group: 3, val: 15 }
      ],
      links: [
        { source: "00后", target: "二次元" },
        { source: "00后", target: "游戏" },
        { source: "00后", target: "搞钱" },
        { source: "00后", target: "数码" },
        { source: "广东", target: "早茶" },
        { source: "广东", target: "夜宵" },
        { source: "广东", target: "搞钱" },
        { source: "程序员", target: "数码" },
        { source: "程序员", target: "脱发" },
        { source: "程序员", target: "加班" },
        { source: "程序员", target: "键盘" },
        { source: "程序员", target: "搞钱" },
        { source: "游戏", target: "原神" },
        { source: "游戏", target: "王者" },
        { source: "游戏", target: "Switch" },
        { source: "游戏", target: "显卡" },
        { source: "搞钱", target: "基金" },
        { source: "搞钱", target: "副业" },
        { source: "数码", target: "显卡" },
        { source: "数码", target: "键盘" },
        { source: "数码", target: "Switch" },
        { source: "二次元", target: "番剧" },
        { source: "二次元", target: "Coser" },
        { source: "二次元", target: "原神" },
        { source: "加班", target: "咖啡" },
        { source: "撸猫", target: "咖啡" }
      ]
    }

    this.setData({
      graphData: mockGraphData,
      canvasReady: true
    })
  },

  // 初始化图谱
  initGraph() {
    const that = this
    const query = wx.createSelectorQuery().in(this)

    query.select('#graph-canvas')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (res[0]) {
          const canvas = res[0].node
          const ctx = canvas.getContext('2d')
          const dpr = wx.getSystemInfoSync().pixelRatio

          canvas.width = res[0].width * dpr
          canvas.height = res[0].height * dpr
          ctx.scale(dpr, dpr)

          // 保存画布信息
          that.canvasInfo = {
            canvas: canvas,
            ctx: ctx,
            width: res[0].width,
            height: res[0].height,
            dpr: dpr
          }

          // 启动图谱渲染（传入 canvas 实例用于 requestAnimationFrame）
          that.renderGraph(canvas, ctx, res[0].width, res[0].height)
        }
      })
  },

  // 渲染图谱
  renderGraph(canvas, ctx, width, height) {
    const { graphData } = this.data
    if (!graphData) return

    const that = this

    // 力导向算法参数
    const centerX = width / 2
    const centerY = height / 2
    const repulsionStrength = 3000
    const attractionStrength = 0.01
    const damping = 0.9
    let animationId = null
    let isDragging = false
    let dragNode = null
    let highlightedNodes = new Set()
    let highlightedLinks = new Set()
    let linkAnimProgress = {} // 连线动画进度
    let nodeScales = {} // 节点缩放

    // 初始化节点位置和速度
    const nodes = graphData.nodes.map(node => ({
      ...node,
      x: centerX + (Math.random() - 0.5) * 200,
      y: centerY + (Math.random() - 0.5) * 200,
      vx: 0,
      vy: 0,
      radius: Math.sqrt(node.val) * 2,
      baseRadius: Math.sqrt(node.val) * 2, // 保存基础半径
      scale: 1 // 缩放比例
    }))

    // 保存节点数据供触摸事件使用
    this.graphNodes = nodes

    // 🔥 粒子系统
    const particles = []
    let particleIdCounter = 0

    // 创建连线索引映射
    const linksMap = {}
    graphData.links.forEach(link => {
      const sourceNode = nodes.find(n => n.id === link.source)
      const targetNode = nodes.find(n => n.id === link.target)
      if (sourceNode && targetNode) {
        const key = `${link.source}-${link.target}`
        linksMap[key] = { source: sourceNode, target: targetNode }
      }
    })

    // 颜色映射
    const colors = {
      1: '#22d3ee',  // 青色
      2: '#a78bfa',  // 紫色
      3: '#64748b'   // 灰色
    }

    // 力导向计算
    const updateForces = () => {
      // 斥力
      for (let i = 0; i < nodes.length; i++) {
        // 如果节点被固定（拖动中），跳过
        if (nodes[i].fx !== null && nodes[i].fx !== undefined) continue

        for (let j = i + 1; j < nodes.length; j++) {
          const dx = nodes[j].x - nodes[i].x
          const dy = nodes[j].y - nodes[i].y
          const distance = Math.sqrt(dx * dx + dy * dy) || 1
          const force = repulsionStrength / (distance * distance)
          const fx = (dx / distance) * force
          const fy = (dy / distance) * force

          nodes[i].vx -= fx
          nodes[i].vy -= fy

          // 如果节点j未被固定，才施加力
          if (nodes[j].fx === null || nodes[j].fx === undefined) {
            nodes[j].vx += fx
            nodes[j].vy += fy
          }
        }
      }

      // 引力（连线）
      Object.values(linksMap).forEach(link => {
        const dx = link.target.x - link.source.x
        const dy = link.target.y - link.source.y
        const distance = Math.sqrt(dx * dx + dy * dy) || 1
        const force = distance * attractionStrength
        const fx = (dx / distance) * force
        const fy = (dy / distance) * force

        // 只对未固定的节点施加力
        if (link.source.fx === null || link.source.fx === undefined) {
          link.source.vx += fx
          link.source.vy += fy
        }
        if (link.target.fx === null || link.target.fx === undefined) {
          link.target.vx -= fx
          link.target.vy -= fy
        }
      })

      // 中心引力和位置更新
      nodes.forEach(node => {
        // 如果节点被固定（拖动中），使用固定位置
        if (node.fx !== null && node.fx !== undefined) {
          node.x = node.fx
          node.y = node.fy
          node.vx = 0
          node.vy = 0
          return
        }

        const dx = centerX - node.x
        const dy = centerY - node.y
        node.vx += dx * 0.01
        node.vy += dy * 0.01

        // 速度衰减
        node.vx *= damping
        node.vy *= damping

        // 更新位置
        node.x += node.vx
        node.y += node.vy

        // 边界限制
        node.x = Math.max(node.baseRadius + 10, Math.min(width - node.baseRadius - 10, node.x))
        node.y = Math.max(node.baseRadius + 10, Math.min(height - node.baseRadius - 10, node.y))
      })
    }

    // 绘制函数
    let frameCount = 0
    let isAnimating = true

    const draw = () => {
      // 清空画布
      ctx.fillStyle = '#020617'
      ctx.fillRect(0, 0, width, height)

      // 绘制连线
      Object.entries(linksMap).forEach(([linkKey, link]) => {
        const isHighlighted = highlightedLinks.has(linkKey)
        const hasAnimation = linkAnimProgress[linkKey] !== undefined

        // 更新连线动画进度
        if (hasAnimation && linkAnimProgress[linkKey] < 1) {
          linkAnimProgress[linkKey] = Math.min(1, linkAnimProgress[linkKey] + 0.06)
        }

        const animProgress = linkAnimProgress[linkKey] || 1 // 默认为1（完整显示）

        // 设置连线样式
        if (isHighlighted) {
          // 高亮连线（青色）
          ctx.strokeStyle = '#22d3ee'
          ctx.lineWidth = 2.5

          // 动画中：透明度和长度都渐变
          if (hasAnimation && animProgress < 1) {
            ctx.globalAlpha = 0.1 + animProgress * 0.9 // 0.3 → 0.9
          } else {
            ctx.globalAlpha = 0.9
          }
        } else {
          // 普通连线
          ctx.strokeStyle = '#3a4258'
          ctx.lineWidth = 1
          ctx.globalAlpha = highlightedNodes.size > 0 ? 0.15 : 0.6
        }

        // 绘制连线
        if (isHighlighted && hasAnimation && animProgress < 1) {
          // 动画效果：从起点到终点逐渐连接
          const currentX = link.source.x + (link.target.x - link.source.x) * animProgress
          const currentY = link.source.y + (link.target.y - link.source.y) * animProgress

          ctx.beginPath()
          ctx.moveTo(link.source.x, link.source.y)
          ctx.lineTo(currentX, currentY)
          ctx.stroke()

          // 绘制连接点发光效果
          if (animProgress > 0.1) {
            ctx.save()
            ctx.globalAlpha = 1

            // 外圈光晕（粉色）
            const glowGradient = ctx.createRadialGradient(currentX, currentY, 0, currentX, currentY, 8)
            glowGradient.addColorStop(0, 'rgba(236, 72, 153, 0.8)')
            glowGradient.addColorStop(1, 'rgba(236, 72, 153, 0)')
            ctx.fillStyle = glowGradient
            ctx.beginPath()
            ctx.arc(currentX, currentY, 8, 0, Math.PI * 2)
            ctx.fill()

            // 核心亮点
            ctx.fillStyle = '#ec4899'
            ctx.beginPath()
            ctx.arc(currentX, currentY, 2.5, 0, Math.PI * 2)
            ctx.fill()

            ctx.restore()
          }
        } else {
          // 完整显示连线
          ctx.beginPath()
          ctx.moveTo(link.source.x, link.source.y)
          ctx.lineTo(link.target.x, link.target.y)
          ctx.stroke()
        }
      })
      ctx.globalAlpha = 1

      // 🔥 绘制粒子（在节点之前，让节点覆盖粒子）
      particles.forEach((particle, index) => {
        // 更新粒子进度
        particle.progress += particle.speed

        // 如果粒子到达终点，移除它
        if (particle.progress >= 1) {
          particles.splice(index, 1)
          return
        }

        // 线性插值计算当前位置
        const currentX = particle.startX + (particle.endX - particle.startX) * particle.progress
        const currentY = particle.startY + (particle.endY - particle.startY) * particle.progress

        // 绘制粒子发光效果（外圈光晕）
        const gradient = ctx.createRadialGradient(currentX, currentY, 0, currentX, currentY, particle.size * 3)
        gradient.addColorStop(0, 'rgba(34, 211, 238, 0.8)')
        gradient.addColorStop(0.5, 'rgba(34, 211, 238, 0.3)')
        gradient.addColorStop(1, 'rgba(34, 211, 238, 0)')

        ctx.fillStyle = gradient
        ctx.beginPath()
        ctx.arc(currentX, currentY, particle.size * 3, 0, Math.PI * 2)
        ctx.fill()

        // 绘制粒子核心
        ctx.fillStyle = '#ffffff'
        ctx.beginPath()
        ctx.arc(currentX, currentY, particle.size, 0, Math.PI * 2)
        ctx.fill()
      })

      // 绘制节点
      nodes.forEach(node => {
        const color = colors[node.group] || '#64748b'
        const isHighlighted = highlightedNodes.has(node.id)
        const isDimmed = highlightedNodes.size > 0 && !isHighlighted

        // 更新节点缩放动画
        const targetScale = isHighlighted ? 1.3 : (isDimmed ? 0.9 : 1)
        node.scale += (targetScale - node.scale) * 0.15 // 平滑过渡
        node.radius = node.baseRadius * node.scale

        // 光晕（带缩放）
        ctx.fillStyle = color
        ctx.globalAlpha = isDimmed ? 0.03 : (isHighlighted ? 0.4 : 0.15)
        ctx.beginPath()
        ctx.arc(node.x, node.y, node.radius * 1.5, 0, Math.PI * 2)
        ctx.fill()

        // 核心圆
        ctx.globalAlpha = isDimmed ? 0.2 : 1
        // 渐变背景色（适应新背景）
        const nodeGradient = ctx.createRadialGradient(node.x, node.y, 0, node.x, node.y, node.radius)
        nodeGradient.addColorStop(0, '#1a1b3a')
        nodeGradient.addColorStop(1, '#0f1419')
        ctx.fillStyle = nodeGradient
        ctx.beginPath()
        ctx.arc(node.x, node.y, node.radius, 0, Math.PI * 2)
        ctx.fill()

        ctx.strokeStyle = color
        ctx.lineWidth = isHighlighted ? 3.5 : 2
        ctx.stroke()

        // 高亮外圈（呼吸效果）
        if (isHighlighted) {
          const pulseAlpha = 0.3 + Math.sin(Date.now() / 300) * 0.2
          ctx.strokeStyle = '#fff'
          ctx.lineWidth = 1.5
          ctx.globalAlpha = pulseAlpha
          ctx.beginPath()
          ctx.arc(node.x, node.y, node.radius + 4, 0, Math.PI * 2)
          ctx.stroke()

          ctx.globalAlpha = pulseAlpha * 0.5
          ctx.beginPath()
          ctx.arc(node.x, node.y, node.radius + 7, 0, Math.PI * 2)
          ctx.stroke()
        }

        // 文字（带缩放）
        ctx.globalAlpha = isDimmed ? 0.3 : 1
        ctx.fillStyle = node.group === 1 ? '#ffffff' : (isHighlighted ? '#22d3ee' : '#cbd5e1')
        ctx.font = `${Math.max(10, node.radius / 1.5)}px sans-serif`
        ctx.textAlign = 'center'
        ctx.textBaseline = 'top'
        ctx.fillText(node.id, node.x, node.y + node.radius + 5)
        ctx.globalAlpha = 1
      })

      // 更新力并继续动画
      if (isAnimating) {
        if (frameCount < 100 || isDragging) {
          updateForces()
          frameCount++
        }
        // 持续渲染（用于拖动和高亮效果）
        animationId = canvas.requestAnimationFrame(draw)
      }
    }

    // 启动动画
    draw()

    // 🔥 粒子生成器 - 每 150ms 生成一个粒子
    const particleInterval = setInterval(() => {
      // 如果动画已停止，清理定时器
      if (!isAnimating) {
        clearInterval(particleInterval)
        return
      }

      // 随机选择一条连线
      const linkKeys = Object.keys(linksMap)
      if (linkKeys.length === 0) return

      const randomLinkKey = linkKeys[Math.floor(Math.random() * linkKeys.length)]
      const link = linksMap[randomLinkKey]

      // 随机决定粒子方向（50% 概率反向）
      const reverse = Math.random() > 0.5
      const startNode = reverse ? link.target : link.source
      const endNode = reverse ? link.source : link.target

      // 创建粒子
      particles.push({
        id: particleIdCounter++,
        startX: startNode.x,
        startY: startNode.y,
        endX: endNode.x,
        endY: endNode.y,
        progress: 0,
        speed: 0.008 + Math.random() * 0.006, // 速度：0.008-0.014 (相当于 1000-1400ms)
        size: 1.5 + Math.random() * 2 // 大小：1.5-3.5px
      })
    }, 150)

    // 保存状态以便后续清理和交互
    this.graphCanvas = canvas
    this.graphContext = {
      nodes,
      linksMap,
      isDragging: () => isDragging,
      setDragging: (val) => { isDragging = val },
      dragNode: () => dragNode,
      setDragNode: (node) => { dragNode = node },
      highlightedNodes,
      highlightedLinks,
      stopAnimation: () => {
        isAnimating = false
        clearInterval(particleInterval)
      },
      colors,
      particleInterval
    }
  },

  // Canvas 触摸开始
  onGraphTouchStart(e) {
    if (!this.canvasInfo || !this.graphContext) return

    const touch = e.touches[0]
    const { x, y } = this.getTouchPosition(touch)
    const { nodes, setDragging, setDragNode } = this.graphContext

    // 记录触摸起点位置，用于判断是点击还是拖动
    this.touchStartPos = { x, y }
    this.touchStartTime = Date.now()

    // 查找点击的节点（扩大触摸区域到 15px）
    const clickedNode = nodes.find(node => {
      const dx = x - node.x
      const dy = y - node.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      // 触摸区域至少 30px，或节点半径 + 15px（取较大值）
      const touchRadius = Math.max(30, node.baseRadius + 15)
      return distance <= touchRadius
    })

    if (clickedNode) {
      // 开始拖动
      setDragging(true)
      setDragNode(clickedNode)

      // 完全固定节点位置并清除速度
      clickedNode.fx = x
      clickedNode.fy = y
      clickedNode.x = x
      clickedNode.y = y
      clickedNode.vx = 0
      clickedNode.vy = 0
    }
  },

  // Canvas 触摸移动
  onGraphTouchMove(e) {
    if (!this.canvasInfo || !this.graphContext) return

    const { isDragging, dragNode } = this.graphContext
    if (!isDragging() || !dragNode()) return

    const touch = e.touches[0]
    const { x, y } = this.getTouchPosition(touch)
    const node = dragNode()

    // 直接设置节点位置，清除速度，确保平滑跟随
    node.x = x
    node.y = y
    node.fx = x
    node.fy = y
    node.vx = 0
    node.vy = 0
  },

  // Canvas 触摸结束
  onGraphTouchEnd(e) {
    if (!this.canvasInfo || !this.graphContext) return

    const { isDragging, dragNode, setDragging, setDragNode, nodes, linksMap, highlightedNodes, highlightedLinks } = this.graphContext

    if (isDragging()) {
      const node = dragNode()
      if (node) {
        // 释放节点
        node.fx = null
        node.fy = null

        // 判断是点击还是拖动
        const touch = e.changedTouches[0]
        const { x, y } = this.getTouchPosition(touch)

        // 计算触摸起点和终点的距离
        const moveDistance = this.touchStartPos ?
          Math.sqrt(
            Math.pow(x - this.touchStartPos.x, 2) +
            Math.pow(y - this.touchStartPos.y, 2)
          ) : 0

        // 计算触摸时长
        const touchDuration = Date.now() - (this.touchStartTime || 0)

        // 如果移动距离小于 20px 且时长小于 500ms，判定为点击
        if (moveDistance < 20 && touchDuration < 500) {
          // 是点击而非拖动，高亮关联网络
          this.highlightNetwork(node)
        }
      }

      setDragging(false)
      setDragNode(null)
    } else {
      // 点击空白处，取消高亮
      highlightedNodes.clear()
      highlightedLinks.clear()
      this.setData({ showNodeCard: false, selectedNode: null })
    }

    // 清理触摸记录
    this.touchStartPos = null
    this.touchStartTime = null
  },

  // 高亮关联网络
  highlightNetwork(node) {
    if (!this.graphContext) return

    const { nodes, linksMap, highlightedNodes, highlightedLinks } = this.graphContext

    // 清空之前的高亮
    highlightedNodes.clear()
    highlightedLinks.clear()

    // 添加当前节点
    highlightedNodes.add(node.id)

    // 查找所有相关节点和连线
    let connections = 0
    Object.entries(linksMap).forEach(([key, link]) => {
      if (link.source.id === node.id) {
        highlightedNodes.add(link.target.id)
        highlightedLinks.add(key)
        connections++
      } else if (link.target.id === node.id) {
        highlightedNodes.add(link.source.id)
        highlightedLinks.add(key)
        connections++
      }
    })

    // 显示统计卡片
    this.setData({
      showNodeCard: true,
      selectedNode: {
        id: node.id,
        name: node.id,
        group: node.group,
        value: node.val,
        connections: connections,
        relatedNodes: Array.from(highlightedNodes).filter(id => id !== node.id)
      }
    })
  },

  // 获取触摸位置（考虑 Canvas 缩放）
  getTouchPosition(touch) {
    const { width, height, dpr } = this.canvasInfo
    return {
      x: touch.x,
      y: touch.y
    }
  },

  // 关闭节点卡片
  closeNodeCard() {
    if (!this.graphContext) return

    const { highlightedNodes, highlightedLinks } = this.graphContext
    highlightedNodes.clear()
    highlightedLinks.clear()

    // 清理连线动画进度
    if (this.graphContext.linkAnimProgress) {
      Object.keys(this.graphContext.linkAnimProgress).forEach(key => {
        delete this.graphContext.linkAnimProgress[key]
      })
    }

    this.setData({
      showNodeCard: false,
      selectedNode: null
    })
  },

  // 页面卸载时清理动画
  onUnload() {
    /* if (this.graphContext) {
      this.graphContext.stopAnimation()
    }
    if (this.graphCanvas) {
      try {
        this.graphCanvas.cancelAnimationFrame()
      } catch (e) {
        // 忽略错误
      }
    } */
  },

  // 点击加入/退出按钮
  toggleJoin() {
    const { associationInfo } = this.data
    const { applicationStatus } = associationInfo

    // 根据申请状态显示不同提示或执行不同操作
    switch (applicationStatus) {
      case 0: // 待审核 - 进入申请详情页面（查看模式）
        this.goToApplicationDetailPage()
        break

      case 1: // 已通过（已加入）
        wx.showModal({
          title: '退出校友会',
          content: '确定要退出该校友会吗？',
          confirmText: '确定退出',
          confirmColor: '#40B2E6',
          success: (res) => {
            if (res.confirm) {
              // TODO: 调用退出校友会接口
              wx.showToast({
                title: '已退出',
                icon: 'success'
              })
              // 更新状态
              this.setData({
                'associationInfo.applicationStatus': null
              })
            }
          }
        })
        break

      case 2: // 已拒绝
        wx.showModal({
          title: '申请被拒绝',
          content: '您的申请已被拒绝，是否重新申请？',
          confirmText: '重新申请',
          confirmColor: '#40B2E6',
          success: (res) => {
            if (res.confirm) {
              this.goToApplyPage()
            }
          }
        })
        break



      case null: // 未申请
      default:
        this.goToApplyPage()
        break
    }
  },

  // 跳转到申请页面
  goToApplyPage() {
    const { associationInfo } = this.data
    const schoolId = associationInfo.schoolId || ''
    const schoolName = associationInfo.schoolName || ''
    wx.navigateTo({
      url: `/pages/alumni-association/apply/apply?id=${this.data.associationId}&schoolId=${schoolId}&schoolName=${encodeURIComponent(schoolName)}`
    })
  },

  // 跳转到申请详情页面（查看模式）
  goToApplicationDetailPage() {
    wx.navigateTo({
      url: `/pages/alumni-association/apply/apply?id=${this.data.associationId}&mode=view`
    })
  },

  // 关闭申请弹窗
  closeJoinModal() {
    if (this.data.joinSubmitting) return
    this.setData({ showJoinModal: false })
  },

  // 表单输入绑定
  handleJoinInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      joinForm: {
        ...this.data.joinForm,
        [field]: value
      }
    })
  },

  // 提交加入申请
  async submitJoinApplication() {
    const { associationId, joinForm, joinSubmitting, associationInfo } = this.data
    if (joinSubmitting) return

    if (!joinForm.realName) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' })
      return
    }

    this.setData({ joinSubmitting: true })
    try {
      // 当前后端 join 接口未要求表单字段，这里先直接提交申请
      const res = await associationApi.joinAssociation(associationId)
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '申请已提交，待审核',
          icon: 'success'
        })
        this.setData({
          showJoinModal: false,
          associationInfo: {
            ...associationInfo,
            isJoined: true
          }
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '提交失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ joinSubmitting: false })
    }
  },

  // 查看通知详情
  viewNotificationDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/notification/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/notification/detail/detail?id=${id}`
      })
    }
  },

  // 查看活动详情
  viewActivityDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/detail-new/detail-new?id=${id}`
    })
  },

  // 查看权益详情
  viewBenefitDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/benefit/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/benefit/detail/detail?id=${id}`
      })
    }
  },

  // 认证标签点击
  handleCertificationTap(e) {
    const { type, id } = e.currentTarget.dataset
    if (!type || !id) return

    if (type === 'union') {
      wx.navigateTo({
        url: `/pages/alumni-union/detail/detail?id=${id}`
      })
    } else if (type === 'platform') {
      wx.navigateTo({
        url: `/pages/local-platform/detail/detail?id=${id}`
      })
    } else if (type === 'promotion') {
      wx.navigateTo({
        url: `/pages/promotion/detail/detail?id=${id}`
      })
    }
  },

  // 查看母校详情
  viewSchoolDetail() {
    const { associationInfo } = this.data
    if (!associationInfo || !associationInfo.schoolId) {
      wx.showToast({
        title: '暂无母校信息',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${associationInfo.schoolId}`
    })
  },

  // 查看校友企业详情
  viewEnterpriseDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/enterprise/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/enterprise/detail/detail?id=${id}`
      })
    }
  },

  // 查看校友商铺详情
  viewShopDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/shop/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/shop/detail/detail?id=${id}`
      })
    }
  },

  // 查看组织架构
  viewOrganizationStructure() {
    // 不再跳转，改为加载组织结构数据
    this.loadOrganizationTree()
  },

  // 加载组织架构树
  loadOrganizationTree() {
    this.setData({
      organizationLoading: true
    })

    // 调用API获取组织架构树
    const { post } = require('../../../utils/request.js')
    post('/AlumniAssociation/organizationTree/v2', {
      alumniAssociationId: this.data.associationId
    }).then(res => {
      if (res.data && res.data.code === 200) {
        this.setData({
          roleList: res.data.data || []
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
        organizationLoading: false
      })
    })
  }
})
