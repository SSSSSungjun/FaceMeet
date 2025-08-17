<template>
  <nav>
    <button 
      v-for="tab in tabs" 
      :key="tab.id"
      :class="{ active: activeTab === tab.id }"
      @click="$emit('tab-change', tab.id)"
    >
      {{ tab.name }}
    </button>
  </nav>
</template>

<script>
export default {
  name: 'Navigation',
  props: {
    tabs: {
      type: Array,
      required: true
    },
    activeTab: {
      type: String,
      required: true
    }
  },
  emits: ['tab-change']
}
</script>

<style scoped>
nav {
  display: flex;
  justify-content: center;
  background-color: white;
  border-bottom: 1px solid #F2F2F2;
}

nav button {
  flex: 1;
  padding: 12px;
  font-size: 15px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
}

nav button.active {
  color: #333;
  font-weight: bold;
  border-color: #333;
}

nav button:hover {
  background-color: #f5f5f5;
}
</style>
