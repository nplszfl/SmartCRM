package com.smartcrm.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartcrm.ai.dto.request.EmailGenerateRequest;
import com.smartcrm.ai.dto.response.EmailGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailGenerationService {

    private final LlmClientService llmClient;

    private static final Map<String, EmailTemplate> TEMPLATES = Map.of(
            "initial_outreach", new EmailTemplate(
                    "Quick question about {company_name}",
                    "Idea for {company_name}",
                    """
                    Hi {contact_name},

                    I noticed {company_name} is in the {industry} space, and I've been working with similar companies to help them {value_proposition}.

                    Would you be open to a quick 15-minute call this week? I'd love to share some ideas that could help {specific_benefit}.

                    Best,
                    {sender_name}
                    """,
                    """
                    Hi {contact_name},

                    Your work at {company_name} caught my attention - especially {personalization_topic}. This aligns with something we've helped peers in {industry} achieve: {specific_benefit}.

                    Would you have 15 minutes to explore if this could work for your team?

                    Best,
                    {sender_name}
                    """
            ),
            "follow_up", new EmailTemplate(
                    "Following up - {company_name}",
                    "Checking in - {company_name}",
                    """
                    Hi {contact_name},

                    I wanted to follow up on my previous message about {topic}. I understand you're busy, so I'll keep this brief.

                    {social_proof} - this helped similar companies achieve {benefit}.

                    Would you be open to a quick call to discuss?

                    Best,
                    {sender_name}
                    """,
                    """
                    Hi {contact_name},

                    Curious if you saw my last note. I wanted to share how {company_similar} used {solution} to {benefit}.

                    If now's not the right time, just let me know - I'm happy to reconnect later.

                    Best,
                    {sender_name}
                    """
            ),
            "meeting_request", new EmailTemplate(
                    "Meeting request: {topic}",
                    "Quick sync on {topic}?",
                    """
                    Hi {contact_name},

                    I'd like to request a meeting to discuss how we can help {company_name} achieve {goal}.

                    Based on what I've learned about your situation, I suggest we cover:
                    - {agenda_item_1}
                    - {agenda_item_2}
                    - {agenda_item_3}

                    What does your calendar look like this week?

                    Best,
                    {sender_name}
                    """,
                    """
                    Hi {contact_name},

                    I hope this message finds you well. I'd love to set up a brief call to share some ideas specifically for {company_name}.

                    Do you have 20 minutes this week for a quick conversation?

                    Best,
                    {sender_name}
                    """
            ),
            "demo_request", new EmailTemplate(
                    "Demo request for {company_name}",
                    "See {solution} in action",
                    """
                    Hi {contact_name},

                    Thank you for your interest in {solution}! I'd love to schedule a personalized demo for your team.

                    I'll show you how {company_similar} achieved {result} in just {timeframe}.

                    Which of these times works best for you?
                    - {time_option_1}
                    - {time_option_2}
                    - {time_option_3}

                    Best,
                    {sender_name}
                    """,
                    """
                    Hi {contact_name},

                    Excited to show you what {solution} can do! Our demo typically takes 30 minutes and covers:
                    - Live walkthrough of key features
                    - Custom scenarios for {industry}
                    - Q&A with our team

                    Ready to book a time? Here are some openings: {time_option_1} or {time_option_2}.

                    Best,
                    {sender_name}
                    """
            ),
            "proposal", new EmailTemplate(
                    "Proposal for {company_name}",
                    "Custom solution for {company_name}",
                    """
                    Hi {contact_name},

                    As discussed, I'm attaching our proposal for {company_name}.

                    Key highlights:
                    - {proposal_highlight_1}
                    - {proposal_highlight_2}
                    - {proposal_highlight_3}

                    Let me know if you have any questions or would like to schedule a call to walk through the details.

                    Best,
                    {sender_name}
                    """,
                    """
                    Hi {contact_name},

                    I've put together a tailored proposal for {company_name} based on your specific goals: {goals_summary}.

                    I'd welcome the opportunity to review this with you directly and answer any questions.

                    Best,
                    {sender_name}
                    """
            )
    );

    public EmailGenerateResponse generateEmail(EmailGenerateRequest request) {
        log.info("Generating email for lead: {}", request.getLeadId());

        EmailTemplate template = TEMPLATES.getOrDefault(
                request.getTemplateType(),
                TEMPLATES.get("initial_outreach")
        );

        EmailPersonalizationData data = buildPersonalizationData(request);

        String subjectA = template.subjectA()
                .replace("{company_name}", data.companyName)
                .replace("{topic}", data.topic);

        String bodyA = fillTemplate(template.bodyA(), data);
        String bodyB = null;
        String subjectB = null;

        if (Boolean.TRUE.equals(request.getAbTesting())) {
            subjectB = template.subjectB()
                    .replace("{company_name}", data.companyName)
                    .replace("{topic}", data.topic);
            bodyB = fillTemplate(template.bodyB(), data);
        }

        try {
            if (hasRichPersonalization(request)) {
                bodyA = enhanceWithLlm(bodyA, data, "A");
                if (bodyB != null) {
                    bodyB = enhanceWithLlm(bodyB, data, "B");
                }
            }
        } catch (Exception e) {
            log.warn("LLM enhancement failed, using template: {}", e.getMessage());
        }

        return EmailGenerateResponse.builder()
                .leadId(request.getLeadId())
                .subjectA(subjectA)
                .subjectB(subjectB)
                .variantA(bodyA)
                .variantB(bodyB)
                .templateUsed(request.getTemplateType())
                .build();
    }

    private EmailPersonalizationData buildPersonalizationData(EmailGenerateRequest request) {
        EmailGenerateRequest.EmailPersonalizationData pd = request.getPersonalizationData();

        return new EmailPersonalizationData(
                request.getContactName(),
                request.getCompanyName(),
                request.getContactEmail(),
                pd != null && pd.getSenderName() != null ? pd.getSenderName() : "Your Name",
                pd != null && pd.getIndustry() != null ? pd.getIndustry() : "your industry",
                pd != null && pd.getValueProposition() != null ? pd.getValueProposition() : "improve efficiency and reduce costs",
                pd != null && pd.getSpecificBenefit() != null ? pd.getSpecificBenefit() : "significant ROI",
                pd != null && pd.getTopic() != null ? pd.getTopic() : "how we can help",
                pd != null && pd.getSocialProof() != null ? pd.getSocialProof() : "We've worked with 50+ companies",
                pd != null && pd.getBenefit() != null ? pd.getBenefit() : "improve their processes",
                pd != null && pd.getCompanySimilar() != null ? pd.getCompanySimilar() : "similar companies",
                pd != null && pd.getSolution() != null ? pd.getSolution() : "our solution",
                pd != null && pd.getGoalsSummary() != null ? pd.getGoalsSummary() : "your goals",
                pd != null && pd.getTimeframe() != null ? pd.getTimeframe() : "90 days",
                pd != null && pd.getResult() != null ? pd.getResult() : "impressive results",
                pd != null && pd.getAgendaItem1() != null ? pd.getAgendaItem1() : "current challenges",
                pd != null && pd.getAgendaItem2() != null ? pd.getAgendaItem2() : "potential solutions",
                pd != null && pd.getAgendaItem3() != null ? pd.getAgendaItem3() : "next steps",
                pd != null && pd.getTimeOption1() != null ? pd.getTimeOption1() : "Tuesday 2pm",
                pd != null && pd.getTimeOption2() != null ? pd.getTimeOption2() : "Wednesday 3pm",
                pd != null && pd.getTimeOption3() != null ? pd.getTimeOption3() : "Thursday 10am",
                pd != null && pd.getProposalHighlight1() != null ? pd.getProposalHighlight1() : "Customized approach",
                pd != null && pd.getProposalHighlight2() != null ? pd.getProposalHighlight2() : "Competitive pricing",
                pd != null && pd.getProposalHighlight3() != null ? pd.getProposalHighlight3() : "Fast implementation",
                pd != null && pd.getPersonalizationTopic() != null ? pd.getPersonalizationTopic() : "your team's approach",
                pd != null && pd.getGoal() != null ? pd.getGoal() : "your goals"
        );
    }

    private boolean hasRichPersonalization(EmailGenerateRequest request) {
        EmailGenerateRequest.EmailPersonalizationData pd = request.getPersonalizationData();
        return pd != null && (
                pd.getPersonalizationTopic() != null ||
                pd.getSocialProof() != null ||
                pd.getCompanySimilar() != null
        );
    }

    private String enhanceWithLlm(String bodyTemplate, EmailPersonalizationData data, String variant) {
        String prompt = String.format("""
                Generate a personalized sales email based on this template and data.

                TEMPLATE:
                %s

                PERSONALIZATION DATA:
                - contact_name: %s
                - company_name: %s
                - contact_email: %s
                - sender_name: %s
                - industry: %s
                - value_proposition: %s
                - specific_benefit: %s
                - topic: %s
                - social_proof: %s
                - benefit: %s
                - company_similar: %s
                - solution: %s

                VARIANT: %s

                Generate the complete email body, filling in all placeholders with the personalization data.
                Keep the email professional, concise, and focused on value.
                Make sure the tone is appropriate for B2B sales.
                Return JSON: {"email": "<generated email>"}
                """,
                bodyTemplate,
                data.contactName,
                data.companyName,
                data.contactEmail,
                data.senderName,
                data.industry,
                data.valueProposition,
                data.specificBenefit,
                data.topic,
                data.socialProof,
                data.benefit,
                data.companySimilar,
                data.solution,
                variant
        );

        try {
            JsonNode result = llmClient.completeJson(prompt, llmClient.getSystemPromptForType("email_generation"));
            if (result.has("email")) {
                return result.get("email").asText();
            }
        } catch (Exception e) {
            log.warn("LLM email enhancement failed: {}", e.getMessage());
        }

        return bodyTemplate;
    }

    private String fillTemplate(String template, EmailPersonalizationData data) {
        return template
                .replace("{contact_name}", data.contactName)
                .replace("{company_name}", data.companyName)
                .replace("{contact_email}", data.contactEmail)
                .replace("{sender_name}", data.senderName)
                .replace("{industry}", data.industry)
                .replace("{value_proposition}", data.valueProposition)
                .replace("{specific_benefit}", data.specificBenefit)
                .replace("{topic}", data.topic)
                .replace("{social_proof}", data.socialProof)
                .replace("{benefit}", data.benefit)
                .replace("{company_similar}", data.companySimilar)
                .replace("{solution}", data.solution)
                .replace("{goals_summary}", data.goalsSummary)
                .replace("{timeframe}", data.timeframe)
                .replace("{result}", data.result)
                .replace("{agenda_item_1}", data.agendaItem1)
                .replace("{agenda_item_2}", data.agendaItem2)
                .replace("{agenda_item_3}", data.agendaItem3)
                .replace("{time_option_1}", data.timeOption1)
                .replace("{time_option_2}", data.timeOption2)
                .replace("{time_option_3}", data.timeOption3)
                .replace("{proposal_highlight_1}", data.proposalHighlight1)
                .replace("{proposal_highlight_2}", data.proposalHighlight2)
                .replace("{proposal_highlight_3}", data.proposalHighlight3)
                .replace("{personalization_topic}", data.personalizationTopic)
                .replace("{goal}", data.goal);
    }

    private record EmailTemplate(String subjectA, String subjectB, String bodyA, String bodyB) {}

    private record EmailPersonalizationData(
            String contactName,
            String companyName,
            String contactEmail,
            String senderName,
            String industry,
            String valueProposition,
            String specificBenefit,
            String topic,
            String socialProof,
            String benefit,
            String companySimilar,
            String solution,
            String goalsSummary,
            String timeframe,
            String result,
            String agendaItem1,
            String agendaItem2,
            String agendaItem3,
            String timeOption1,
            String timeOption2,
            String timeOption3,
            String proposalHighlight1,
            String proposalHighlight2,
            String proposalHighlight3,
            String personalizationTopic,
            String goal
    ) {}
}