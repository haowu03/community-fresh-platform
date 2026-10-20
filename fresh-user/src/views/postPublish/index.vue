<template>
  <div class="page">
    <NavBar title="发帖" />
    <van-form @submit="onSubmit">
      <van-cell-group inset title="帖子类型">
        <van-field>
          <template #input>
            <van-radio-group v-model="form.type" direction="horizontal">
              <van-radio :name="1" checked-color="#39b57f">团购分享</van-radio>
              <van-radio :name="2" checked-color="#39b57f">好物分享</van-radio>
            </van-radio-group>
          </template>
        </van-field>
      </van-cell-group>

      <van-cell-group inset>
        <van-field v-model="form.title" label="标题" placeholder="请输入标题" maxlength="50"
          :rules="[{ required: true, message: '请输入标题' }]" />
        <van-field v-model="form.content" label="内容" type="textarea" rows="6" maxlength="1000" show-word-limit
          placeholder="分享你的团购/好物体验" :rules="[{ required: true, message: '请输入内容' }]" />
      </van-cell-group>

      <!-- 图片：后端用户端无上传接口，简化为填写图片 URL -->
      <van-cell-group inset title="图片（选填，填图片URL）">
        <van-field v-for="(img, i) in form.images" :key="i" v-model="form.images[i]" placeholder="https:// 或 /upload/xxx">
          <template #right-icon>
            <van-icon name="cross" color="#999" @click="form.images.splice(i, 1)" />
          </template>
        </van-field>
        <van-cell v-if="form.images.length < 9" title="添加图片URL" icon="plus" is-link title-style="color:#39b57f"
          @click="form.images.push('')" />
      </van-cell-group>

      <div v-if="previewUrls.length" class="previews">
        <van-image v-for="(u, i) in previewUrls" :key="i" :src="imgUrl(u)" width="72" height="72" radius="6" fit="cover" />
      </div>

      <div class="submit-wrap">
        <van-button round block type="success" native-type="submit" :loading="submitting">发布</van-button>
        <p class="tip">发布后需管理员审核，审核通过后才会展示在社区</p>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { publishPost } from '../../api/post'
import { imgUrl } from '../../utils/format'

const router = useRouter()
const submitting = ref(false)
const form = reactive({ type: 1, title: '', content: '', images: [] })

const previewUrls = computed(() => form.images.filter(u => u && u.trim()))

async function onSubmit() {
  submitting.value = true
  try {
    // images 为 JSON 数组字符串
    await publishPost({
      type: form.type,
      title: form.title,
      content: form.content,
      images: JSON.stringify(previewUrls.value)
    })
    showToast('发布成功，待管理员审核')
    router.back()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.previews {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 24px;
}
.submit-wrap {
  padding: 16px;
}
.tip {
  text-align: center;
  color: #999;
  font-size: 12px;
}
</style>
