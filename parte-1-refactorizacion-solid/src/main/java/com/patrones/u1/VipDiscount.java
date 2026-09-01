package com.patrones.u1;

public class VipDiscount implements DiscountStrategy {
    @Override
    public double apply(double total) {
        return total * 0.85; // 10% de descuento para clientes VIP
    }
    
}
