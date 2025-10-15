package com.bytz.cms.payment.interfaces;

import com.bytz.cms.payment.application.CreditRepaymentApplicationService;
import com.bytz.cms.payment.interfaces.model.ro.PaymentCreateRO;
import com.bytz.cms.payment.interfaces.model.vo.PaymentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 信用还款控制器
 * Credit Repayment Controller
 * 
 * 职责：处理信用还款相关HTTP请求
 * 相关用例：UC-PM-007, UC-PM-008
 */
@RestController
@RequestMapping("/api/credit-repayment")
@Validated
public class CreditRepaymentController {
    
    @Autowired
    private CreditRepaymentApplicationService creditRepaymentApplicationService;
    
    /**
     * 创建信用还款支付单
     * UC-PM-007：创建信用还款支付单
     * 
     * @param request 创建请求
     * @return 支付单信息
     */
    @PostMapping("/create")
    public PaymentVO createCreditRepaymentPayment(@Valid @RequestBody PaymentCreateRO request) {
        // TODO: Implement controller logic
        // 1. 接收CreatePaymentRO（paymentType=CREDIT_REPAYMENT）
        // 2. 调用CreditRepaymentApplicationService.createCreditRepaymentPayment()
        // 3. 返回PaymentVO
        
        throw new UnsupportedOperationException("Method not implemented");
    }
}
