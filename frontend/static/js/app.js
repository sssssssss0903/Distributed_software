// 秒杀系统前端脚本 - 动静分离演示
document.addEventListener("DOMContentLoaded", function () {
  const healthBtn = document.getElementById("healthBtn");
  const healthResult = document.getElementById("healthResult");
  const loadProductsBtn = document.getElementById("loadProductsBtn");
  const productList = document.getElementById("productList");

  healthBtn.addEventListener("click", async function () {
    healthResult.textContent = "请求中...";
    try {
      const res = await fetch("/api/user/health");
      const data = await res.json();
      healthResult.textContent = JSON.stringify(data, null, 2);
    } catch (e) {
      healthResult.textContent = "请求失败: " + e.message;
    }
  });

  loadProductsBtn.addEventListener("click", async function () {
    productList.innerHTML = "加载中...";
    try {
      const res = await fetch("/api/product/list");
      const data = await res.json();
      if (data.code === 200 && data.data) {
        productList.innerHTML =
          data.data
            .map(
              (p) =>
                `<div class="product-item"><strong>${p.name}</strong> - ¥${p.price}</div>`
            )
            .join("") || "暂无商品";
      } else {
        productList.textContent = "加载失败";
      }
    } catch (e) {
      productList.textContent = "请求失败: " + e.message;
    }
  });
});
