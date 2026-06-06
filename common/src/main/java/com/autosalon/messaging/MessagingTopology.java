package com.autosalon.messaging;

public final class MessagingTopology {
    public static final String EXCHANGE = "autosalon.orders";
    public static final String ORDER_APPROVAL_REQUEST_QUEUE = "order.approval.requests";
    public static final String ORDER_APPROVAL_RESULT_QUEUE = "order.approval.results";
    public static final String ORDER_APPROVAL_REQUEST_ROUTING_KEY = "order.approval.requested";
    public static final String ORDER_APPROVAL_RESULT_ROUTING_KEY = "order.approval.completed";

    private MessagingTopology() {
    }
}
