<template>
    <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
        <div style="flex: 1">
            <el-image style="width: 100%;height: 100%" fit="cover"
                      src="https://ts1.tc.mm.bing.net/th/id/R-C.f18b25f703b00848e4d40352140d2e29?rik=h5SsubvvDUjPDg&riu=http%3a%2f%2fpic.qianye88.com%2f4kyingshicf2e2f90-8760-11ea-b4e2-8c85903ee22c.jpg&ehk=qE6TVTF88UhfaiwGAJwm2F6IS6IxTeFUeVtnsAfWcFo%3d&risl=&pid=ImgRaw&r=0"/>
        </div>
        <div class="welcome-title">
            <div style="font-size: 30px;font-weight: bold">欢迎来到租赁平台</div>
            <div style="margin-top: 10px">在这里你租到很多很多的东西。</div>
            <div style="margin-top: 5px">价格实惠</div>
        </div>
        <div class="right-card">
            <router-view v-slot="{ Component }">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<script setup>

</script>

<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

.welcome-title {
    position: absolute;
    bottom: 30px;
    left: 30px;
    color: white;
    text-shadow: 0 0 10px black;
}
</style>
