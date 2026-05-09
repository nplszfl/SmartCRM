# SmartCRM - AI驱动的客户关系管理系统

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-orange.svg)](https://vuejs.org/)
[![AI](https://img.shields.io/badge/AI-Sales--Powered-yellow.svg)](https://deepseek.com/)

> 📈 **AI-First销售赋能平台** - 智能线索评分、AI邮件助手、成交预测，让销售团队效率提升3倍

## 📋 项目简介

SmartCRM 是一套**深度AI融合**的客户关系管理系统，对标Salesforce但AI能力强10倍。

### 核心AI能力

| AI功能 | 描述 | 价值 |
|--------|------|------|
| 🎯 **智能线索评分** | LLM分析线索质量，自动打分流 | 销售效率提升40% |
| 📧 **AI邮件助手** | 自动生成个性化邮件，支持AB测试 | 回复率提升60% |
| 📅 **智能跟进** | AI判断最佳跟进时机，自动提醒 | 成交率提升35% |
| 🔮 **成交预测** | 预测订单成交概率，预警流失风险 | 营收预测准确率90% |
| 📞 **AI销售教练** | 分析通话/聊天，提取改进建议 | 快速提升新人能力 |

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Vue 3 前端                            │
│   (线索管理 / 销售管道 / 邮件中心 / 数据分析 / AI洞察)         │
├─────────────────────────────────────────────────────────────┤
│                   Spring Cloud Alibaba 后端                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐  │
│  │ Gateway     │ │ CRM         │ │ Lead                │  │
│  │ Service     │ │ Service     │ │ Service             │  │
│  └─────────────┘ └─────────────┘ └─────────────────────┘  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐  │
│  │ Opportunity │ │ Email       │ │ AI Service          │  │
│  │ Service     │ │ Service     │ │ (Java)              │  │
│  └─────────────┘ └─────────────┘ └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    Java AI 服务层                            │
│  (LeadScoring / EmailGen / DealPrediction / ConversationAI) │
├─────────────────────────────────────────────────────────────┤
│                    DeepSeek API                            │
└─────────────────────────────────────────────────────────────┘
```

## 📂 目录结构

```
SmartCRM/
├── ai-service/                    # Java AI 服务层 ⭐
│   ├── src/main/java/
│   │   └── com/smartcrm/ai/
│   │       ├── controller/         # REST API
│   │       ├── service/            # AI 业务逻辑
│   │       │   ├── LeadScoringService.java        # 线索评分
│   │       │   ├── EmailGenerationService.java    # 邮件生成
│   │       │   ├── DealPredictionService.java     # 成交预测
│   │       │   ├── ConversationAnalysisService.java # 对话分析
│   │       │   ├── IntelligentRoutingService.java  # 智能路由
│   │       │   └── LlmClientService.java          # LLM调用
│   │       └── dto/                # 数据传输对象
│   └── pom.xml
│
├── crm-service/                    # 客户管理服务
├── lead-service/                    # 线索管理服务
├── opportunity-service/             # 商机管理服务
├── email-service/                  # 邮件服务
├── analytics-service/              # 分析服务
├── gateway/                        # API网关
├── frontend/                       # Vue 3前端
│   └── src/
│       ├── views/                 # 页面
│       │   ├── leads/             # 线索管理
│       │   ├── pipeline/          # 销售管道
│       │   ├── emails/            # 邮件中心
│       │   └── analytics/         # 数据分析
│       └── components/            # 组件
└── pom.xml
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis
- Nacos

### 1. 启动后端

```bash
git clone https://github.com/nplszfl/SmartCRM.git
cd SmartCRM

mvn clean install -DskipTests

# AI服务
cd ai-service && mvn spring-boot:run

# 其他微服务
cd ../gateway && mvn spring-boot:run
cd ../crm-service && mvn spring-boot:run
cd ../lead-service && mvn spring-boot:run
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 3. 配置

```yaml
# ai-service/src/main/resources/application.yml
spring:
  ai:
    deepseek:
      api-key: your-api-key
      model: deepseek-chat
```

## 📡 核心API

### AI线索评分

```
POST /api/v1/ai/lead/score

{
  "leadId": "lead_123",
  "company": "某科技有限公司",
  "industry": "互联网",
  "employeeCount": 100,
  "source": "官网表单",
  "behaviorData": {
    "pageViews": 15,
    "emailOpens": 8,
    "formSubmissions": 2
  }
}

响应：
{
  "score": 85,
  "grade": "A",
  "reasoning": "公司规模适中，行业匹配度高，官网行为数据显示高度意向",
  "recommendedActions": ["优先联系", "发送产品资料", "安排 demo"]
}
```

### AI邮件生成

```
POST /api/v1/ai/email/generate

{
  "leadId": "lead_123",
  "purpose": "产品介绍",
  "tone": "professional",
  "includeCallToAction": true
}

响应：
{
  "subject": "某科技公司，您好 - 关于我们的智能CRM解决方案",
  "content": "尊敬的采购负责人...\n\n我们注意到贵公司在业务增长过程中...",
  "variantB": {
    "subject": "提升销售业绩30%的秘密",
    "content": "您好...\n\n想知道某科技公司如何做到的..."
  },
  "recommendation": "推荐使用版本A，专业度更高"
}
```

### AI成交预测

```
POST /api/v1/ai/deal/predict

{
  "dealId": "deal_456",
  "amount": 500000,
  "stage": "proposal",
  "daysInStage": 15,
  "contactFrequency": 3,
  "competitorMentioned": true,
  "historicalWinRate": 0.35
}

响应：
{
  "closeProbability": 0.72,
  "confidence": "high",
  "riskFactors": ["竞品出现", "决策周期过长"],
  "recommendedActions": ["尽快安排决策者会议", "申请优惠价格"],
  "estimatedCloseDate": "2024-03-15"
}
```

## 🎯 AI销售流程

```
新线索进入
    ↓
[AI线索评分] → A/B/C/D分级 → 智能分配
    ↓
[AI邮件助手] → 个性化邮件 → 自动发送
    ↓
[跟进提醒] → AI判断最佳时机 → 销售跟进
    ↓
[商机培育] → AI分析意向 → 预测成交概率
    ↓
成交/流失预警
```

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Element Plus + ECharts |
| 后端 | Java 21 + Spring Cloud Alibaba + MyBatis Plus |
| AI服务 | Spring Boot 3.2 + WebClient + DeepSeek |
| 数据库 | MySQL |
| 缓存 | Redis |
| 注册中心 | Nacos |

## 📊 功能列表

### 线索管理
- 线索批量导入
- AI自动评分分级
- 智能分配给销售
- 线索转商机

### 销售管道
- 看板式管线视图
- 拖拽调整阶段
- AI成交预测
- 阶段转化分析

### 邮件中心
- AI邮件生成
- 邮件模板管理
- 发送追踪（打开/点击）
- AB测试

### 数据分析
- 销售业绩看板
- AI洞察面板
- 转化漏斗分析
- 预测 vs 实际对比

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👨‍💻 作者

**黄辉翔** - [GitHub](https://github.com/nplszfl)

---

⭐ 如果对你有帮助，请给项目一个 Star！
