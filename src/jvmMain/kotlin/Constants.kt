import java.util.LinkedList

const val PDF_FOLDER = "C:\\Users\\Administrator\\Desktop\\Document\\"
val PDF_LIST = object : LinkedList<String>() {
    override operator fun get(index: Int): String {
        if (index in 0 until size) return super.get(index)
        var target = index
        if (index < 0) {
            while(target < 0) {
                target += size
            }
        } else {
            while(target >= size) {
                target -= size
            }
        }
        return super.get(target)
    }
}.apply {
    add("Android开发-高博闻-15542648253.pdf")
    add("项目经历.pdf")
    add("StorageSolutionProvider.pdf")
    add("Handler.pdf")
    add("View的Touch事件传递机制.pdf")
    add("Android.pdf")
    add("Java.pdf")
    add("Kotlin.pdf")
    add("三方框架.pdf")
    add("泛型.pdf")
    add("Retrofit.pdf")
    add("xiangxueketang-Java并发编程.pdf")
    add("xiangxueketang-Java泛型与注解.pdf")
    add("xiangxueketang-Java核心基础.pdf")
    add("xiangxueketang-Java虚拟机原理.pdf")
    add("通用问题.pdf")
    add("QA.pdf")
}

private const val PREFIX_VOICE_GBW = """
    假设场景：你是一个Android程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个面试者的角度回答问题。
    问题的文本内容来自于语音识别技术，可能出现近似发音导致的误识别，
        举例：RecyclerView可能被识别为SQL View或被截断为Recycle。
        这可能导致问题晦涩难懂/脱离编程的范畴，尝试根据中文或英文的近似发音来匹配一些Android编程中常见的词汇来理解问题。
        错误如果存在，则大概率出现在文本中英文单词及其前后位置。
        对文本内容的准确度应该始终持悲观态度，优先尝试上述发音匹配策略，在回答首段中给出匹配结果，再对问题做解答。
    下面是问题：
    """
private const val PREFIX_VOICE_MY = """
    假设场景：你是一个Java程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。
    问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应英文词汇来理解问题，一些可能出现的英文单词有：Java、Spring Boot。
    下面是问题：
    """
private const val PREFIX_VOICE_WHY = """
    假设场景：你是一个IOS程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。
    问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应英文词汇来理解问题。
    下面是问题：
    """
private const val PREFIX_VOICE_LYH = """
    假设场景：你是一个互联网产品经理，正在参加招聘面试，现在我向你提出问题，请尝试从一个产品经理的角度回答问题。
    问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应术语词汇来理解问题。
    下面是问题：
"""
val PREFIX_VOICE_MAP = mapOf("GBW" to PREFIX_VOICE_GBW, "MY" to PREFIX_VOICE_MY, "WHY" to PREFIX_VOICE_WHY, "LYH" to PREFIX_VOICE_LYH)

private const val PREFIX_ALGO_GBW = "解答下列算法问题，给出Kotlin实现，关键部分给出中文注释。如果存在多种常见的解决方案，所有解决方案均要给出，优先给出性能更好的实现。当问题可以通过递归和非递归解决时，两种方案均要给出，优先给出递归方案。先给出代码实现，再阐述解题思路。当问题可以通过Java/Kotlin/Android的已有复杂数据结构简单地实现时，不要给出该实现，而是给出更底层的实现代码，如：实现一个选出小于给定值的数据结构时，不要直接使用TreeSet实现，而是具体实现一个平衡二叉树的查找算法。而LinkedList等则可以直接给出"
private const val PREFIX_ALGO_MY = "解答下列算法问题，给出Java实现，关键部分给出中文注释。优先给出性能更好的实现。尽量避免使用递归思想。先给出代码实现，再阐述解题思路。"
private const val PREFIX_ALGO_WHY = "解答下列算法问题，给出ObjectC实现，关键部分给出中文注释。优先给出性能更好的实现。尽量避免使用递归思想。先给出代码实现，再阐述解题思路。"
val PREFIX_ALGO_MAP = mapOf("GBW" to PREFIX_ALGO_GBW, "MY" to PREFIX_ALGO_MY, "WHY" to PREFIX_ALGO_WHY)