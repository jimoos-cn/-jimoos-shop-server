package cn.jimoos.service;

import cn.jimoos.common.exception.BussException;
import cn.jimoos.entity.PaymentEntity;
import cn.jimoos.error.PayError;
import cn.jimoos.factory.PaymentFactory;
import cn.jimoos.form.PayForm;
import cn.jimoos.form.PayRefundForm;
import cn.jimoos.form.PaySearchForm;
import cn.jimoos.payment.model.PaymentVO;
import cn.jimoos.payment.model.RefundVO;
import cn.jimoos.payment.provider.PayProvider;
import cn.jimoos.repository.PaymentRepository;
import cn.jimoos.utils.OutTradeNoEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author :keepcleargas
 * @date :2021-04-20 17:57.
 */
@Service
@Slf4j
public class PaymentService {
    @Resource
    PaymentFactory paymentFactory;
    @Resource
    PaymentRepository paymentRepository;

    /**
     * 支付
     *
     * @param payForm     payForm
     * @param payProvider payProvider
     * @return PaymentVo
     * @throws BussException PayError.PAY_ERROR
     */
    public PaymentVO pay(PayForm payForm, PayProvider payProvider) throws BussException {
        Assert.notNull(payProvider, "支付提供方不能为空");
        String orderNum = payForm.getOrderNum();
        // 支付增量
        PaymentEntity payment = paymentFactory.create(payForm);
        paymentRepository.save(payment);
        PaymentVO payVo;
        try {
            String result = payProvider.pay(OutTradeNoEncoder.encodeOutTradeNo(orderNum), payForm.getSubject(), payForm.getBody(), payForm.getMoney(), payForm.getExtras());
            payVo = new PaymentVO(orderNum, payForm.getSubject(), result, payForm.getPayType());
        } catch (Exception e) {
            if (e instanceof BussException) {
                throw (BussException) e;
            }
            log.error("{}支付失败:{}", payForm.getPayType(), e);
            throw new BussException(PayError.PAY_ERROR);
        }
        return payVo;
    }
}
