package com.example.entity.vo.request;

public class OrderQueryVO {
    private String status;          // 订单状态
    private String paymentStatus;   // 支付状态
    private Integer page = 1;       // 页码
    private Integer size = 10;      // 每页数量

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
