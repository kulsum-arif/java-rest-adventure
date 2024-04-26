package com.synkrato.services.partner.business.webhook;

import com.synkrato.services.epc.common.dto.WebhookDTO;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public interface SubscriptionService {

  List<WebhookDTO> createSubscription(List<Map<String, Object>> createSubscriptionsList);

  WebhookDTO getSubscription(String subscriptionId);

  void updateSubscription(List<Map<String, Object>> updateSubscriptionsList);

  void deleteSubscriptions(List<String> subscriptionIds);
}
