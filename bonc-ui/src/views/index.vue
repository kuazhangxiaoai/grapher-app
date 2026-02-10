<template>
  <div class="welcome-container">
    <div class="welcome-box" style="height: 90vh; width: 80vw;">
      <div class="greeting">
        <h1>您好，{{ username }}</h1>
        <p class="welcome-text">欢迎回来</p>
      </div>

      <div class="time-box">
        <div class="time">{{ currentTime }}</div>
        <div class="date">{{ currentDate }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WelcomePage',
  data() {
    return {
      username: '管理员',
      currentTime: '',
      currentDate: ''
    }
  },
  mounted() {
    this.updateDateTime()
    setInterval(this.updateDateTime, 1000)
  },
  methods: {
    updateDateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
      this.currentDate = now.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    }
  }
}
</script>

<style scoped>
.welcome-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.welcome-box {
  text-align: center;
  padding: 100px;
  border-radius: 16px;
  background-color: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.greeting h1 {
  font-size: 48px;
  color: #2c3e50;
  margin: 0;
  font-weight: 500;
}

.welcome-text {
  font-size: 24px;
  color: #606266;
  margin: 16px 0 40px 0;
}

.time-box {
  margin-top: 20px;
}

.time {
  font-size: 56px;
  color: #409EFF;
  font-weight: 300;
  margin-bottom: 16px;
}

.date {
  font-size: 24px;
  color: #606266;
}
</style>
