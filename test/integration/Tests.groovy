import lt.mediapark.chatmap.utils.DistanceCalc

def rnd = new Random()

Double latOrigin = 54.689566
Double lngOrigin = 25.272500

[1525].each { n ->
    def list = []
    n.times {
        list << [latOrigin - (rnd.nextDouble() / rnd.nextInt(10000)), lngOrigin + (rnd.nextDouble() / rnd.nextInt(10000))]
    }

    def centerPoint = list[rnd.nextInt(list.size())]

    def nanosM = System.nanoTime()
    list.each { point ->
        DistanceCalc.getMetricDistance(*centerPoint, *point)
    }
    nanosM = (System.nanoTime() - nanosM)

    nanosH = System.nanoTime()
    list.each { point ->
        DistanceCalc.getHaversineDistance(*centerPoint, *point)
    }
    nanosH = System.nanoTime() - nanosH
    println "Metric took ${nanosM}"
    println "Havershine took ${nanosH}"

    println "For ${n} values, Havershine is ${(1.0 - (nanosH / nanosM)) * 100}% faster"
}

def fact
fact = { n, total ->
    n == 0 ? total : fact.trampoline(n - 1, n * total)
}.trampoline()
//def factorial = { n -> fact(n, 1G)}
println fact(20, 1G) // => 2432902008176640000