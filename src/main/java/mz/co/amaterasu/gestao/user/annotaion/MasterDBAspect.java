package mz.co.amaterasu.gestao.user.annotaion;

import mz.co.amaterasu.gestao.util.TenantContext;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MasterDBAspect {

    @Around("@annotation(MasterDB)")
    public Object switchToMasterDB(ProceedingJoinPoint joinPoint) throws Throwable {
        String previousTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setMasterTenant();
            return joinPoint.proceed();
        } finally {
            if (previousTenant != null) {
                TenantContext.setCurrentTenant(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }
}
