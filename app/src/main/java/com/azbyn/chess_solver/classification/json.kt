package com.azbyn.chess_solver.classification

import android.content.Context
import java.io.File
import kotlinx.serialization.json.Json

/*
inline fun <reified T> readFromJson(path: String): T {
    val str = File(path).readText()
    return Json.decodeFromString<T>(string = str)
}
inline fun <reified T> readFromJsonCtx(ctx: Context, path: String): T {
    val str = ctx.assets.open(path).reader().readText()

//    val str = File(path).readText()
    return Json.decodeFromString<T>(string = str)
}
inline fun <reified T> writeToJson(path: String, t: T) =
    File(path).writeText(Json { prettyPrint = true }.encodeToString(t))

 */