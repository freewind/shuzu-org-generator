import _ from 'lodash'

function splitByFirst(text: string, keyword: string): undefined | [string, string] {
    const result = text.split(new RegExp(`${keyword}(.*)`))
    if (result === undefined || result.length !== 3) {
        return undefined
    } else {
        return [result[0], result[1]]
    }
}

function matches(strings: string[], keyword: string): boolean {
    const result = [...strings]
    for (const index in strings) {
        const str = strings[index]
        const split = splitByFirst(str, keyword)
        if (split !== undefined) {
            result.splice(parseInt(index), 1, ...split.filter(x => !_.isEmpty(x)))
            return true
        }
    }
    return false
}

export function splitKeywords(keyword: string): string[] {
    const keywords = _.uniq(keyword.trim().toLowerCase().split(/\s+/)).sort((a, b) => {
        if (a.length === b.length) {
            return a > b ? 1 : -1
        } else {
            return a.length - b.length
        }
    })
    return keywords.filter(x => !_.isEmpty(x))
}


export function matchesKeywords(text: string, keywords: string[]): boolean {
    const parts = [text.toLowerCase()]
    for (const keyword of keywords) {
        if (!matches(parts, keyword)) {
            return false
        }
    }
    return true
}

type MatchResult = {
    content: string
    matched: boolean
}

function matchKeyword(parts: MatchResult[], keyword: string) {
    for (const index in parts) {
        const part = parts[index]
        if (part.matched) continue

        const split = splitByFirst(part.content, keyword)
        if (split !== undefined) {
            const [head, tail] = split
            const newParts = [
                {content: head, matched: false},
                {content: keyword, matched: true},
                {content: tail, matched: false}
            ].filter(x => !_.isEmpty(x.content))
            parts.splice(parseInt(index), 1, ...newParts)
            return
        }
    }
}

export function matchResult(text: string, keywords: string[]): MatchResult[] {
    const parts = [{
        content: text.toLowerCase(),
        matched: false
    }]
    for (const keyword of keywords) {
        matchKeyword(parts, keyword)
    }
    return parts
}