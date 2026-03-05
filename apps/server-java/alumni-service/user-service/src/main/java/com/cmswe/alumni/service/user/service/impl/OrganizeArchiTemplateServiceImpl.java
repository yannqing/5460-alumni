package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.OrganizeArchiTemplateService;
import com.cmswe.alumni.common.entity.OrganizeArchiTemplate;
import com.cmswe.alumni.common.vo.OrganizeArchiTemplateVo;
import com.cmswe.alumni.service.user.mapper.OrganizeArchiTemplateMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织架构模板服务实现类
 */
@Slf4j
@Service
public class OrganizeArchiTemplateServiceImpl
        extends ServiceImpl<OrganizeArchiTemplateMapper, OrganizeArchiTemplate>
        implements OrganizeArchiTemplateService {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取所有可用的组织架构模板列表
     *
     * @param organizeType 组织类型（可选，不传则返回所有类型）
     * @return 模板列表
     */
    @Override
    public List<OrganizeArchiTemplateVo> getAllTemplates(Integer organizeType) {
        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<OrganizeArchiTemplate> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(organizeType != null, OrganizeArchiTemplate::getOrganizeType, organizeType)
                    .eq(OrganizeArchiTemplate::getStatus, 1) // 只查询启用的模板
                    .orderByDesc(OrganizeArchiTemplate::getIsDefault) // 默认模板排在前面
                    .orderByAsc(OrganizeArchiTemplate::getCreateTime);

            // 2. 查询模板列表
            List<OrganizeArchiTemplate> templates = this.list(queryWrapper);

            // 3. 转换为VO
            return templates.stream()
                    .map(this::convertToVo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取组织架构模板列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 将模板实体转换为VO
     *
     * @param template 模板实体
     * @return 模板VO
     */
    private OrganizeArchiTemplateVo convertToVo(OrganizeArchiTemplate template) {
        try {
            OrganizeArchiTemplateVo vo = new OrganizeArchiTemplateVo();
            vo.setTemplateId(String.valueOf(template.getTemplateId()));
            vo.setTemplateName(template.getTemplateName());
            vo.setTemplateCode(template.getTemplateCode());
            vo.setOrganizeType(template.getOrganizeType());
            vo.setDescription(template.getDescription());
            vo.setIsDefault(template.getIsDefault());

            // 解析JSON字符串为树形结构
            if (template.getTemplateJson() != null && !template.getTemplateJson().isEmpty()) {
                List<Map<String, Object>> flatNodes = objectMapper.readValue(
                        template.getTemplateJson(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                // 将扁平结构转换为树形结构
                List<OrganizeArchiTemplateVo.TemplateNode> treeNodes = buildTree(flatNodes);
                vo.setTemplateContent(treeNodes);
            }

            return vo;

        } catch (Exception e) {
            log.error("转换模板VO失败, templateId={}", template.getTemplateId(), e);
            return new OrganizeArchiTemplateVo();
        }
    }

    /**
     * 将扁平结构转换为树形结构
     *
     * @param flatNodes 扁平节点列表
     * @return 树形节点列表
     */
    private List<OrganizeArchiTemplateVo.TemplateNode> buildTree(List<Map<String, Object>> flatNodes) {
        // 1. 转换为TemplateNode对象，并建立映射
        Map<String, OrganizeArchiTemplateVo.TemplateNode> nodeMap = new HashMap<>();
        List<OrganizeArchiTemplateVo.TemplateNode> allNodes = new ArrayList<>();

        for (Map<String, Object> nodeData : flatNodes) {
            OrganizeArchiTemplateVo.TemplateNode node = new OrganizeArchiTemplateVo.TemplateNode();

            // 处理nodeId，转为String
            Object nodeIdObj = nodeData.get("nodeId");
            String nodeId = nodeIdObj != null ? String.valueOf(nodeIdObj) : null;
            node.setNodeId(nodeId);

            // 处理pid，转为String
            Object pidObj = nodeData.get("pid");
            String pid = pidObj != null && !"null".equals(String.valueOf(pidObj))
                    ? String.valueOf(pidObj) : null;
            node.setPid(pid);

            node.setRoleOrName((String) nodeData.get("roleOrName"));
            node.setRoleOrCode((String) nodeData.get("roleOrCode"));
            node.setRemark((String) nodeData.get("remark"));
            node.setChildren(new ArrayList<>());

            if (nodeId != null) {
                nodeMap.put(nodeId, node);
            }
            allNodes.add(node);
        }

        // 2. 构建树形结构
        List<OrganizeArchiTemplateVo.TemplateNode> rootNodes = new ArrayList<>();

        for (OrganizeArchiTemplateVo.TemplateNode node : allNodes) {
            if (node.getPid() == null || node.getPid().isEmpty() || "null".equals(node.getPid())) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点，添加到父节点的children中
                OrganizeArchiTemplateVo.TemplateNode parentNode = nodeMap.get(node.getPid());
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                } else {
                    // 如果找不到父节点，则作为根节点
                    rootNodes.add(node);
                }
            }
        }

        return rootNodes;
    }
}
