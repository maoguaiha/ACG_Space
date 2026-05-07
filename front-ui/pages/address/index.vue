<template>
  <div class="min-h-screen pb-20">
    <!-- Header Section -->
    <section class="relative py-12 overflow-hidden">
      <!-- Background Glow -->
      <div class="absolute inset-0 bg-gradient-to-b from-emerald-900/20 via-slate-900 to-slate-900"></div>
      <div class="absolute top-20 left-1/4 w-[400px] h-[400px] bg-emerald-600/10 rounded-full blur-[120px]"></div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-2xl mx-auto">
          <div class="text-center mb-8">
            <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider bg-gradient-to-r from-emerald-400/20 to-teal-500/20 text-emerald-400 border border-emerald-400/30 mb-4">
              <span class="text-lg">📍</span>
              ADDRESS MANAGEMENT
            </span>
            <h1 class="text-4xl font-black text-white mb-2">收货地址</h1>
            <p class="text-slate-400">管理您的收货地址，用于实体核销发货</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Address List Section -->
    <section class="container mx-auto px-4">
      <div class="max-w-2xl mx-auto">
        <!-- Add New Button -->
        <button
          @click="openAddModal"
          class="w-full py-4 mb-6 rounded-2xl font-bold bg-gradient-to-r from-emerald-500/10 to-teal-500/10 text-emerald-400 border border-emerald-500/30 hover:border-emerald-400/50 transition-all flex items-center justify-center gap-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          添加新地址
        </button>

        <!-- Address Cards -->
        <div class="space-y-4">
          <!-- Loading State -->
          <div v-if="loadingAddresses" class="space-y-4">
            <div v-for="i in 3" :key="i" class="bg-slate-800/50 backdrop-blur-sm rounded-2xl border border-slate-700/50 p-5 animate-pulse">
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-xl bg-slate-700/50"></div>
                  <div>
                    <div class="h-4 bg-slate-700/50 rounded w-24 mb-2"></div>
                    <div class="h-3 bg-slate-700/50 rounded w-32"></div>
                  </div>
                </div>
              </div>
              <div class="h-3 bg-slate-700/50 rounded w-3/4 mb-4"></div>
              <div class="h-3 bg-slate-700/50 rounded w-1/4"></div>
            </div>
          </div>

          <!-- Error State -->
          <div v-else-if="errorAddresses" class="text-center py-16">
            <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-red-500/10 flex items-center justify-center">
              <span class="text-5xl">❌</span>
            </div>
            <h3 class="text-xl font-bold text-white mb-2">加载失败</h3>
            <p class="text-slate-400 mb-6">{{ errorAddresses }}</p>
            <button @click="fetchAddresses" class="px-6 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all">
              重新加载
            </button>
          </div>

          <!-- Address Cards -->
          <template v-else>
            <div
              v-for="address in addresses"
              :key="address.id"
              class="bg-slate-800/50 backdrop-blur-sm rounded-2xl border border-slate-700/50 p-5 transition-all hover:border-emerald-500/30"
              :class="{ 'border-emerald-500/50': address.isDefault }"
            >
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-xl bg-emerald-500/20 flex items-center justify-center">
                    <span class="text-lg">🏠</span>
                  </div>
                  <div>
                    <div class="flex items-center gap-2">
                      <h3 class="font-bold text-white">{{ address.receiver }}</h3>
                      <span v-if="address.isDefault" class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-400">默认</span>
                    </div>
                    <p class="text-sm text-slate-400">{{ address.phone }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <button @click="openEditModal(address)" class="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700 transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button @click="confirmDelete(address)" class="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-700 transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  </button>
                </div>
              </div>

              <p class="text-sm text-slate-400 mb-4">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}</p>

              <div class="flex items-center justify-between">
                <button
                  v-if="!address.isDefault"
                  @click="setDefault(address)"
                  class="text-sm text-slate-500 hover:text-emerald-400 transition-colors"
                >
                  设为默认地址
                </button>
                <span v-else class="text-sm text-emerald-400">默认收货地址</span>
                <NuxtLink
                  :to="`/redeem?addressId=${address.id}`"
                  class="px-4 py-2 rounded-lg text-sm font-medium bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/30 transition-colors"
                >
                  申请核销
                </NuxtLink>
              </div>
            </div>
          </template>
        </div>

        <!-- Empty State -->
        <div v-if="!loadingAddresses && !errorAddresses && addresses.length === 0" class="text-center py-16">
          <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-slate-800/50 flex items-center justify-center">
            <span class="text-5xl">📍</span>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">暂无收货地址</h3>
          <p class="text-slate-400 mb-6">添加收货地址用于实体核销发货</p>
          <button
            @click="openAddModal"
            class="px-6 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all"
          >
            添加地址
          </button>
        </div>
      </div>
    </section>

    <!-- Add/Edit Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeModal">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <div class="p-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-xl font-black text-white">{{ isEditing ? '编辑地址' : '添加地址' }}</h3>
                <button @click="closeModal" class="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center text-slate-400 hover:text-white transition-colors">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                </button>
              </div>

              <!-- Form -->
              <div class="space-y-4">
                <div>
                  <label class="block text-sm text-slate-400 mb-2">收货人</label>
                  <input
                    v-model="form.receiver"
                    type="text"
                    class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
                    placeholder="请输入收货人姓名"
                  />
                </div>
                <div>
                  <label class="block text-sm text-slate-400 mb-2">手机号码</label>
                  <input
                    v-model="form.phone"
                    type="tel"
                    class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
                    placeholder="请输入手机号码"
                  />
                </div>
                <div class="grid grid-cols-3 gap-3">
                  <div>
                    <label class="block text-sm text-slate-400 mb-2">省份</label>
                    <input
                      v-model="form.province"
                      type="text"
                      class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
                      placeholder="省"
                    />
                  </div>
                  <div>
                    <label class="block text-sm text-slate-400 mb-2">城市</label>
                    <input
                      v-model="form.city"
                      type="text"
                      class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
                      placeholder="市"
                    />
                  </div>
                  <div>
                    <label class="block text-sm text-slate-400 mb-2">区县</label>
                    <input
                      v-model="form.district"
                      type="text"
                      class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
                      placeholder="区"
                    />
                  </div>
                </div>
                <div>
                  <label class="block text-sm text-slate-400 mb-2">详细地址</label>
                  <textarea
                    v-model="form.detail"
                    rows="2"
                    class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors resize-none"
                    placeholder="请输入详细地址"
                  ></textarea>
                </div>
                <div class="flex items-center gap-3">
                  <input
                    v-model="form.isDefault"
                    type="checkbox"
                    id="default-address"
                    class="w-5 h-5 rounded border-slate-600 bg-slate-900 text-emerald-500 focus:ring-emerald-500 focus:ring-offset-0"
                  />
                  <label for="default-address" class="text-sm text-slate-400">设为默认收货地址</label>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex gap-3 mt-6">
                <button
                  @click="closeModal"
                  class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  取消
                </button>
                <button
                  @click="saveAddress"
                  :disabled="!canSave"
                  class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  保存
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

    <!-- Delete Confirm Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="showDeleteConfirm" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="cancelDelete">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-sm bg-slate-800/90 backdrop-blur-xl rounded-2xl border border-slate-700/50 p-6" @click.stop>
            <div class="text-center mb-6">
              <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-rose-500/20 flex items-center justify-center">
                <span class="text-3xl">🗑️</span>
              </div>
              <h3 class="text-lg font-bold text-white mb-2">确认删除</h3>
              <p class="text-slate-400 text-sm">确定要删除这个收货地址吗？此操作无法撤销。</p>
            </div>
            <div class="flex gap-3">
              <button
                @click="cancelDelete"
                class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
              >
                取消
              </button>
              <button
                @click="deleteAddress"
                class="flex-1 py-3 rounded-xl font-bold bg-rose-500 text-white hover:bg-rose-600 transition-colors"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { addressApi, type UserAddress } from '~/composables/useV2Api'

interface Address {
  id: string
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
}

const addresses = ref<Address[]>([])
const loadingAddresses = ref(false)
const errorAddresses = ref('')

function mapUserAddressToAddress(userAddr: UserAddress): Address {
  return {
    id: String(userAddr.id),
    receiver: userAddr.receiver,
    phone: userAddr.phone,
    province: userAddr.province,
    city: userAddr.city,
    district: userAddr.district,
    detail: userAddr.detailAddress,
    isDefault: userAddr.isDefault
  }
}

async function fetchAddresses() {
  loadingAddresses.value = true
  errorAddresses.value = ''
  try {
    const result = await addressApi.fetchList()
    addresses.value = (result || []).map(mapUserAddressToAddress)
  } catch (e: any) {
    errorAddresses.value = e.message || '获取地址列表失败'
    console.error('fetchAddresses error:', e)
  } finally {
    loadingAddresses.value = false
  }
}

onMounted(() => {
  fetchAddresses()
})

// Form State
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref('')
const form = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const canSave = computed(() => {
  return form.value.receiver.trim() &&
    form.value.phone.trim() &&
    form.value.province.trim() &&
    form.value.city.trim() &&
    form.value.district.trim() &&
    form.value.detail.trim()
})

// Delete State
const showDeleteConfirm = ref(false)
const deletingId = ref('')

function openAddModal() {
  isEditing.value = false
  editingId.value = ''
  form.value = {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  }
  showModal.value = true
}

function openEditModal(address: Address) {
  isEditing.value = true
  editingId.value = address.id
  form.value = {
    receiver: address.receiver,
    phone: address.phone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detail,
    isDefault: address.isDefault
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function saveAddress() {
  if (!canSave.value) return

  try {
    if (isEditing.value) {
      await addressApi.update(Number(editingId.value), {
        receiver: form.value.receiver,
        phone: form.value.phone,
        province: form.value.province,
        city: form.value.city,
        district: form.value.district,
        detailAddress: form.value.detail,
        isDefault: form.value.isDefault
      })
      const index = addresses.value.findIndex(a => a.id === editingId.value)
      if (index !== -1) {
        addresses.value[index] = {
          ...addresses.value[index],
          ...form.value
        }
      }
    } else {
      await addressApi.create({
        receiver: form.value.receiver,
        phone: form.value.phone,
        province: form.value.province,
        city: form.value.city,
        district: form.value.district,
        detailAddress: form.value.detail,
        isDefault: form.value.isDefault
      })
      await fetchAddresses()
    }
    closeModal()
  } catch (e: any) {
    alert(e.message || '保存失败')
  }
}

async function setDefault(address: Address) {
  try {
    await addressApi.setDefault(Number(address.id))
    addresses.value.forEach(a => a.isDefault = false)
    address.isDefault = true
  } catch (e: any) {
    alert(e.message || '设置默认地址失败')
  }
}

function confirmDelete(address: Address) {
  deletingId.value = address.id
  showDeleteConfirm.value = true
}

function cancelDelete() {
  showDeleteConfirm.value = false
  deletingId.value = ''
}

async function deleteAddress() {
  try {
    await addressApi.delete(Number(deletingId.value))
    addresses.value = addresses.value.filter(a => a.id !== deletingId.value)
    showDeleteConfirm.value = false
    deletingId.value = ''
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}
</script>