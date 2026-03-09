import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router";
import { useUserStore } from "../stores/user";

const authItemName = "authorize"

const accessHeader = () => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    return token ? {
        'Authorization': `Bearer ${token}`
    } : {}
}

const defaultError = (error) => {
    console.error('完整错误:', error)
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '未知错误'
    console.error('错误状态:', status)
    console.error('错误信息:', message)
    if (status === 429) {
        ElMessage.error(error.response.data.message)
    } else if (status === 401) {
        ElMessage.error('登录状态已过期，请重新登录')
        deleteAccessToken(true)
    } else if (status === 403) {
        ElMessage.error('无权限访问')
    } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
    } else if (status === 500) {
        ElMessage.error('服务器内部错误: ' + message)
    } else {
        ElMessage.error('发生了一些错误，请联系管理员')
    }
}

const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if(!str) return null
    const authObj = JSON.parse(str)
    if(new Date(authObj.expire) <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录！")
        return null
    }
    return authObj.token
}

function storeAccessToken(remember, token, expire, userData){
    const authObj = {
        token: token,
        expire: expire
    }
    const str = JSON.stringify(authObj)
    if(remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
    // 同时清理 userStore
    const userStore = useUserStore()
    userStore.logout()
    if(redirect) {
        router.push({ name: 'welcome-login' })
    }
}

function internalPost(url, data, headers, success, failure, error = defaultError){
    axios.post(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure(data.message || '登录状态已过期，请重新登录！', data.code, url)
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalGet(url, headers, success, failure, error = defaultError){
    axios.get(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure(data.message || '登录状态已过期，请重新登录！', data.code, url)
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalPut(url, data, headers, success, failure, error = defaultError){
    axios.put(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure(data.message || '登录状态已过期，请重新登录！', data.code, url)
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalDelete(url, headers, success, failure, error = defaultError){
    axios.delete(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure(data.message || '登录状态已过期，请重新登录！', data.code, url)
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function login(username, password, remember, success, failure = defaultFailure){
    internalPost('/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        // 直接存储 token 到 localStorage/sessionStorage
        if(remember) {
            localStorage.setItem('token', data.token)
        } else {
            sessionStorage.setItem('token', data.token)
        }
        // 更新 userStore
        const userStore = useUserStore()
        userStore.setUser({
            token: data.token,
            userId: data.userId,
            username: data.username,
            email: data.email,
            role: data.role,
            registerTime: data.registerTime
        })
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        success(data)
    }, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), (data) => success(data), failure)
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), (data) => success(data), failure)
}

function put(url, data, success, failure = defaultFailure) {
    internalPut(url, data, accessHeader(), (data) => success(data), failure)
}

function del(url, data, success, failure = defaultFailure) {
    internalDelete(url, accessHeader(), (data) => success(data), failure)
}

function logout(success, failure = defaultFailure){
    get('/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, failure)
}

function unauthorized() {
    const userStore = useUserStore()
    return !userStore.isLoggedIn()
}

// 设备相关API
export function getEquipmentList(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/equipment/list?${query}` : '/equipment/list'
        get(url, resolve, reject)
    })
}

export function getEquipmentDetail(id) {
    return new Promise((resolve, reject) => {
        get(`/equipment/detail/${id}`, resolve, reject)
    })
}

export function addEquipment(data) {
    return new Promise((resolve, reject) => {
        post('/equipment/add', data, resolve, reject)
    })
}

export function updateEquipment(id, data) {
    return new Promise((resolve, reject) => {
        put(`/equipment/update/${id}`, data, resolve, reject)
    })
}

export function deleteEquipment(id) {
    return new Promise((resolve, reject) => {
        del(`/equipment/delete/${id}`, {}, resolve, reject)
    })
}

export function updateEquipmentStatus(id, status) {
    return new Promise((resolve, reject) => {
        put(`/equipment/status/${id}/${status}`, {}, resolve, reject)
    })
}

// 订单相关API
export function getMyOrders(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/order/my-orders?${query}` : '/order/my-orders'
        get(url, resolve, reject)
    })
}

export function getAllOrders(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/order/all-orders?${query}` : '/order/all-orders'
        get(url, resolve, reject)
    })
}

export function getOrderDetail(id) {
    return new Promise((resolve, reject) => {
        get(`/order/detail/${id}`, resolve, reject)
    })
}

export function getOrderByNo(orderNo) {
    return new Promise((resolve, reject) => {
        get(`/order/detail-by-no/${orderNo}`, resolve, reject)
    })
}


export function createOrder(data) {
    return new Promise((resolve, reject) => {
        post('/order/create', data, resolve, reject)
    })
}

export function payOrder(id) {
    return new Promise((resolve, reject) => {
        post(`/order/pay/${id}`, {}, resolve, reject)
    })
}

export function cancelOrder(id) {
    return new Promise((resolve, reject) => {
        post(`/order/cancel/${id}`, {}, resolve, reject)
    })
}

export function completeOrder(id) {
    return new Promise((resolve, reject) => {
        post(`/order/complete/${id}`, {}, resolve, reject)
    })
}

export function returnEquipment(id) {
    return new Promise((resolve, reject) => {
        post(`/order/return/${id}`, {}, resolve, reject)
    })
}

// 评价相关API
export function getEvaluationList(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/evaluation/list?${query}` : '/evaluation/list'
        get(url, resolve, reject)
    })
}

export function getEvaluationByOrderId(orderId) {
    return new Promise((resolve, reject) => {
        get(`/evaluation/order/${orderId}`, resolve, reject)
    })
}

export function addEvaluation(data) {
    return new Promise((resolve, reject) => {
        post('/evaluation/add', data, resolve, reject)
    })
}

export function deleteEvaluation(id) {
    return new Promise((resolve, reject) => {
        post(`/evaluation/delete/${id}`, {}, resolve, reject)
    })
}

// 用户管理API
export function getUserList(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/user/list?${query}` : '/user/list'
        get(url, resolve, reject)
    })
}

export function getUserDetail(id) {
    return new Promise((resolve, reject) => {
        get(`/user/detail/${id}`, resolve, reject)
    })
}

export function updateUserRole(id, role) {
    return new Promise((resolve, reject) => {
        post(`/user/update-role/${id}?role=${role}`, {}, resolve, reject)
    })
}

export function updateUserStatus(id, status) {
    return new Promise((resolve, reject) => {
        post(`/user/update-status/${id}?status=${status}`, {}, resolve, reject)
    })
}

export function getUserStatistics() {
    return new Promise((resolve, reject) => {
        get('/user/statistics', resolve, reject)
    })
}

// 个人信息相关API
export function getCurrentUserProfile() {
    return new Promise((resolve, reject) => {
        get('/user/profile', resolve, reject)
    })
}

export function updateCurrentUserProfile(data) {
    return new Promise((resolve, reject) => {
        put('/user/profile', data, resolve, reject)
    })
}

// 系统管理API
export function getDashboardData() {
    return new Promise((resolve, reject) => {
        get('/system/dashboard', resolve, reject)
    })
}

export function getSystemLogs(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/system/logs?${query}` : '/system/logs'
        get(url, resolve, reject)
    })
}

export function clearSystemLogs() {
    return new Promise((resolve, reject) => {
        post('/system/logs/clear', {}, resolve, reject)
    })
}

export function getSystemSettings() {
    return new Promise((resolve, reject) => {
        get('/system/settings', resolve, reject)
    })
}

export function getSystemSetting(key) {
    return new Promise((resolve, reject) => {
        get(`/system/settings/${key}`, resolve, reject)
    })
}

export function updateSystemSetting(data) {
    return new Promise((resolve, reject) => {
        post('/system/settings/update', data, resolve, reject)
    })
}

export function getBackupList(params) {
    return new Promise((resolve, reject) => {
        const query = new URLSearchParams(params).toString()
        const url = query ? `/system/backups?${query}` : '/system/backups'
        get(url, resolve, reject)
    })
}

export function createBackup(data) {
    return new Promise((resolve, reject) => {
        post('/system/backup/create', data, resolve, reject)
    })
}

export function deleteBackup(id) {
    return new Promise((resolve, reject) => {
        post(`/system/backup/delete/${id}`, {}, resolve, reject)
    })
}

export { post, get, login, logout, unauthorized }

// 配送相关API
export function getDeliveryList(params) {
    return new Promise((resolve, reject) => {
        // 过滤掉 undefined 和 null 值
        const filteredParams = Object.fromEntries(
            Object.entries(params).filter(([_, value]) => value !== undefined && value !== null && value !== '')
        )
        const query = new URLSearchParams(filteredParams).toString()
        const url = query ? `/delivery/list?${query}` : '/delivery/list'
       get(url, resolve, reject)
    })
}

export function getDeliveryDetail(deliveryNo) {
    return new Promise((resolve, reject) => {
        get(`/delivery/detail/${deliveryNo}`, resolve, reject)
    })
}

export function createDelivery(data) {
    return new Promise((resolve, reject) => {
        post('/delivery/create', data, resolve, reject)
    })
}

export function assignCourier(deliveryNo, courierId) {
    return new Promise((resolve, reject) => {
        post(`/delivery/assign?deliveryNo=${deliveryNo}&courierId=${courierId}`, {}, resolve, reject)
    })
}

export function cancelDelivery(deliveryNo) {
    return new Promise((resolve, reject) => {
        post(`/delivery/cancel/${deliveryNo}`, {}, resolve, reject)
    })
}

export function getDeliveryTracking(deliveryNo) {
    return new Promise((resolve, reject) => {
        get(`/delivery/tracking/${deliveryNo}`, resolve, reject)
    })
}

export function startDelivery(deliveryNo) {
    return new Promise((resolve, reject) => {
        post(`/delivery/start/${deliveryNo}`, {}, resolve, reject)
    })
}

export function completeDelivery(deliveryNo) {
    return new Promise((resolve, reject) => {
        post(`/delivery/complete/${deliveryNo}`, {}, resolve, reject)
    })
}

export function getCourierList(params) {
    return new Promise((resolve, reject) => {
        // 过滤掉 undefined 和 null 值
        const filteredParams = Object.fromEntries(
            Object.entries(params || {}).filter(([_, value]) => value !== undefined && value !== null && value !== '')
        )
        const query = new URLSearchParams(filteredParams).toString()
        const url = query ? `/delivery/courier/list?${query}` : '/delivery/courier/list'
        get(url, resolve, reject)
    })
}

// 押金扣除相关API
export function getMyDeductions(params) {
    return new Promise((resolve, reject) => {
        const filteredParams = Object.fromEntries(
            Object.entries(params || {}).filter(([_, value]) => value !== undefined && value !== null && value !== '')
        )
        const query = new URLSearchParams(filteredParams).toString()
        const url = query ? `/deposit-deduction/my-deductions?${query}` : '/deposit-deduction/my-deductions'
        get(url, resolve, reject)
    })
}

export function getAllDeductions(params) {
    return new Promise((resolve, reject) => {
        const filteredParams = Object.fromEntries(
            Object.entries(params || {}).filter(([_, value]) => value !== undefined && value !== null && value !== '')
        )
        const query = new URLSearchParams(filteredParams).toString()
        const url = query ? `/deposit-deduction/all-deductions?${query}` : '/deposit-deduction/all-deductions'
        get(url, resolve, reject)
    })
}

export function getDeductionDetail(id) {
    return new Promise((resolve, reject) => {
        get(`/deposit-deduction/detail/${id}`, resolve, reject)
    })
}

export function createDeduction(data) {
    return new Promise((resolve, reject) => {
        post('/deposit-deduction/create', data, resolve, reject)
    })
}

export function approveDeduction(id) {
    return new Promise((resolve, reject) => {
        post(`/deposit-deduction/approve/${id}`, {}, resolve, reject)
    })
}

export function rejectDeduction(id) {
    return new Promise((resolve, reject) => {
        post(`/deposit-deduction/reject/${id}`, {}, resolve, reject)
    })
}
