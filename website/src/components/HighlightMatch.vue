<template>
    <span>
        <span v-for="part in parts" v-bind:class="{ highlight: part.matched}">{{part.content}}</span>
    </span>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from 'vue-property-decorator'
    import {matchResult} from '../keywords-matching'

    @Component
    export default class HelloWorld extends Vue {
        @Prop() private text!: string
        @Prop() private keywords: string[]

        get parts() {
            return matchResult(this.text, this.keywords)
        }
    }
</script>

<style scoped>
    .highlight {
        color: deeppink;
    }
</style>
