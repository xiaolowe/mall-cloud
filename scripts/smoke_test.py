#!/usr/bin/env python3
"""End-to-end smoke test against the gateway."""
import json
import random
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:9000"


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    request = urllib.request.Request(BASE + path, data=data, method=method)
    request.add_header("Content-Type", "application/json")
    if token:
        request.add_header("Authorization", "Bearer " + token)
    with urllib.request.urlopen(request) as response:
        payload = json.load(response)
    if payload.get("code") != 200:
        raise RuntimeError(f"{method} {path} -> {payload}")
    return payload.get("data")


def stock_of(sku_id, token):
    return call("GET", f"/api/inventory/{sku_id}", token=token)


def main():
    username = "demo" + "".join(random.choices("0123456789", k=6))
    print(f"1. register/login as {username}")
    auth = call("POST", "/api/auth/register", {"username": username, "password": "123456"})
    token = auth["token"]
    print(f"   userId={auth['user']['id']}")

    print("2. products")
    page = call("GET", "/api/products?page=1&size=20")
    products = page["records"]
    assert len(products) == 6, products
    sku_a = call("GET", f"/api/products/{products[0]['id']}")["skus"][0]
    sku_b = call("GET", f"/api/products/{products[1]['id']}")["skus"][0]
    print(f"   {len(products)} products, sku={sku_a['title']} {sku_a['price']}")

    print("3. address")
    call("POST", "/api/users/me/addresses", {
        "receiverName": "测试用户", "receiverPhone": "13800000000",
        "detail": "上海市浦东新区世纪大道 100 号", "isDefault": True
    }, token=token)
    addresses = call("GET", "/api/users/me/addresses", token=token)
    assert addresses and addresses[0]["isDefault"], addresses
    print(f"   {addresses[0]['receiverName']} / {addresses[0]['detail']}")

    before = stock_of(sku_a["id"], token)["available"]
    print(f"4. cart + order (stock before={before})")
    call("POST", "/api/cart", {"skuId": sku_a["id"], "quantity": 2}, token=token)
    cart = call("GET", "/api/cart", token=token)
    assert len(cart) == 1 and cart[0]["quantity"] == 2, cart
    order = call("POST", "/api/orders", {}, token=token)
    order_no = order["orderNo"]
    after = stock_of(sku_a["id"], token)["available"]
    assert after == before - 2, (before, after)
    assert order["status"] == 0 and order["statusText"] == "待支付", order
    print(f"   orderNo={order_no}, total={order['totalAmount']}, stock={after}, status={order['statusText']}")
    assert call("GET", "/api/cart", token=token) == [], "cart should be cleared"

    print("5. pay")
    payment = call("POST", f"/api/payments/{order_no}/pay", token=token)
    assert payment["status"] == 1, payment
    paid_order = call("GET", f"/api/orders/{order_no}", token=token)
    assert paid_order["status"] == 1 and paid_order["statusText"] == "已支付", paid_order
    print(f"   paymentNo={payment['paymentNo']}, order={paid_order['statusText']}")

    print("6. direct order + cancel")
    before = stock_of(sku_b["id"], token)["available"]
    order2 = call("POST", "/api/orders", {"items": [{"skuId": sku_b["id"], "quantity": 1}]}, token=token)
    deducted = stock_of(sku_b["id"], token)["available"]
    assert deducted == before - 1, (before, deducted)
    call("POST", f"/api/orders/{order2['orderNo']}/cancel", token=token)
    restored = stock_of(sku_b["id"], token)["available"]
    assert restored == before, (before, restored)
    cancelled = call("GET", f"/api/orders/{order2['orderNo']}", token=token)
    assert cancelled["status"] == 2, cancelled
    print(f"   stock {before} -> {deducted} -> {restored}, order={cancelled['statusText']}")

    print("7. gateway auth")
    try:
        call("GET", "/api/orders")
        raise AssertionError("unauthenticated request should fail")
    except urllib.error.HTTPError as e:
        assert e.code == 401, e.code
        print("   unauthenticated /api/orders -> 401")

    print("\nSMOKE TEST PASSED")
    print(f"login with {username} / 123456")


if __name__ == "__main__":
    main()
