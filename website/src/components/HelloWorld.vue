<template>
    <div class="hello">
        {{ msg }}
    </div>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from 'vue-property-decorator'

    @Component
    export default class HelloWorld extends Vue {
        @Prop() private msg: string = 'Hello Vue!'
    }
</script>

<style scoped>
    .hello {
        color: red;
    }
</style>
