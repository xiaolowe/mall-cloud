#!/usr/bin/env python3
"""End-to-end test for the admin console APIs."""
import json
import random
import urllib.error
import urllib.parse
import urllib.request

BASE = "http://127.0.0.1:9000"


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    url = BASE + urllib.parse.quote(path, safe="/?&=%:")
    request = urllib.request.Request(url, data=data, method=method)
    request.add_header("Content-Type", "application/json")
    if token:
        request.add_header("Authorization", "Bearer " + token)
    with urllib.request.urlopen(request) as response:
        payload = json.load(response)
    if payload.get("code") != 200:
        raise RuntimeError(f"{method} {path} -> {payload}")
    return payload.get("data")


def main():
    suffix = "".join(random.choices("0123456789", k=4))

    print("1. 分类管理")
    category = call("POST", "/api/admin/categories", {"name": f"测试分类{suffix}", "sort": 99})
    categories = call("GET", "/api/admin/categories")
    assert any(c["id"] == category["id"] for c in categories)
    call("PUT", f"/api/admin/categories/{category['id']}", {"name": f"测试分类{suffix}改", "sort": 98})
    print(f"   分类 id={category['id']} 创建/更新成功")

    print("2. 商品管理")
    product = call("POST", "/api/admin/products", {
        "categoryId": category["id"], "name": f"后台测试商品{suffix}",
        "subtitle": "自动测试", "price": 88.80, "status": 1
    })
    page = call("GET", f"/api/admin/products?page=1&size=50&keyword=后台测试商品{suffix}")
    assert page["total"] == 1, page
    call("PUT", f"/api/admin/products/{product['id']}", {
        "categoryId": category["id"], "name": f"后台测试商品{suffix}改",
        "subtitle": "已修改", "price": 99.90, "status": 1
    })
    print(f"   商品 id={product['id']} 创建/更新成功")

    print("3. SKU + 库存初始化")
    sku = call("POST", "/api/admin/skus", {
        "productId": product["id"], "title": "默认规格", "price": 99.90,
        "specJson": '{"color":"红"}', "initialStock": 50
    })
    stock = call("GET", f"/api/admin/inventory/{sku['id']}")
    assert stock["available"] == 50 and stock["total"] == 50, stock
    print(f"   SKU id={sku['id']}，初始库存 {stock['available']}")

    print("4. 库存调整")
    stock = call("POST", f"/api/admin/inventory/{sku['id']}/adjust?delta=20")
    assert stock["available"] == 70, stock
    stock = call("POST", f"/api/admin/inventory/{sku['id']}/adjust?delta=-10")
    assert stock["available"] == 60, stock
    print(f"   补货+20、扣减-10 后库存 {stock['available']}")

    print("5. 用户下单该商品")
    username = f"admt{suffix}"
    auth = call("POST", "/api/auth/register", {"username": username, "password": "123456"})
    token = auth["token"]
    call("POST", "/api/users/me/addresses", {
        "receiverName": "后台测试", "receiverPhone": "13800000001",
        "detail": "上海市测试路 1 号", "isDefault": True
    }, token=token)
    order = call("POST", "/api/orders", {"items": [{"skuId": sku["id"], "quantity": 3}]}, token=token)
    assert order["totalAmount"] == 299.70, order
    call("POST", f"/api/payments/{order['orderNo']}/pay", token=token)
    print(f"   订单 {order['orderNo']} 已支付，金额 {order['totalAmount']}")

    print("6. 后台发货")
    paid = call("GET", f"/api/admin/orders/{order['orderNo']}")
    assert paid["status"] == 1, paid
    call("POST", f"/api/admin/orders/{order['orderNo']}/ship")
    shipped = call("GET", f"/api/admin/orders/{order['orderNo']}")
    assert shipped["status"] == 3 and shipped["statusText"] == "已完成", shipped
    stock_after = call("GET", f"/api/admin/inventory/{sku['id']}")
    assert stock_after["available"] == 57, stock_after
    print(f"   发货成功，订单状态 {shipped['statusText']}，库存 {stock_after['available']}")

    print("7. 后台关闭待支付订单并回补库存")
    order2 = call("POST", "/api/orders", {"items": [{"skuId": sku["id"], "quantity": 2}]}, token=token)
    call("POST", f"/api/admin/orders/{order2['orderNo']}/close")
    closed = call("GET", f"/api/admin/orders/{order2['orderNo']}")
    assert closed["status"] == 2, closed
    stock_restored = call("GET", f"/api/admin/inventory/{sku['id']}")
    assert stock_restored["available"] == 57, stock_restored
    print(f"   关闭成功，订单 {closed['statusText']}，库存保持在 {stock_restored['available']}")

    print("8. 用户列表")
    users = call("GET", f"/api/admin/users?page=1&size=10&keyword={username}")
    assert users["total"] == 1, users
    print(f"   查到用户 {users['records'][0]['username']}")

    print("9. 看板统计")
    stats = call("GET", "/api/admin/orders/stats")
    assert "salesAmount" in stats and "totalOrders" in stats
    print(f"   总订单 {stats['totalOrders']}，销售额 {stats['salesAmount']}")

    print("10. 清理测试数据")
    call("DELETE", f"/api/admin/skus/{sku['id']}")
    call("DELETE", f"/api/admin/products/{product['id']}")
    call("DELETE", f"/api/admin/categories/{category['id']}")
    print("   测试 SKU/商品/分类已删除")

    print("\n管理后台端到端测试通过")


if __name__ == "__main__":
    main()
