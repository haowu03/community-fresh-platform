package com.fresh.common.constant;

/**
 * Redis 缓存 Key 常量，统一管理防止散落硬编码
 */
public class RedisCacheConstant {

    /** 分类列表 */
    public static final String CATEGORY_LIST = "fresh:category:list";

    /** 分类商品列表前缀：fresh:product:list:{categoryId} */
    public static final String PRODUCT_LIST_PREFIX = "fresh:product:list:";

    /** 商品详情前缀：fresh:product:detail:{id} */
    public static final String PRODUCT_DETAIL_PREFIX = "fresh:product:detail:";

    /** 商品库存前缀：fresh:product:stock:{productId} */
    public static final String PRODUCT_STOCK_PREFIX = "fresh:product:stock:";

    /** 预售活动前缀：fresh:presale:{id} */
    public static final String PRESALE_PREFIX = "fresh:presale:";

    /** 临期特价列表 */
    public static final String NEAR_EXPIRY_LIST = "fresh:near-expiry:list";

    /** 团购活动前缀：fresh:groupbuy:{id} */
    public static final String GROUP_BUY_PREFIX = "fresh:groupbuy:";

    /** 下单防重锁前缀：fresh:order:submit:{userId} */
    public static final String ORDER_SUBMIT_LOCK = "fresh:order:submit:";
}
