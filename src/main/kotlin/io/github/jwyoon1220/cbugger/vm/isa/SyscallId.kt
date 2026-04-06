package io.github.jwyoon1220.cbugger.vm.isa

/**
 * Syscall identifiers for C standard library functions.
 * Used as the operand for the SYSCALL instruction.
 *
 * Calling convention:
 *   - Arguments are pushed left-to-right (first arg pushed first, deepest on stack)
 *   - SYSCALL pops its arguments in reverse order (last arg popped first)
 *   - Return value (if any) is pushed onto the stack
 *   - void functions push nothing
 *
 * For vararg functions (printf, scanf, sprintf):
 *   - Positional args are pushed first, then format string on top
 *   - The handler reads the format string, counts specifiers, pops that many extra args
 */
object SyscallId {
    // ── stdio.h ──────────────────────────────────────────────────────────────
    /** printf(fmt, ...): int — format string on top, args below */
    const val PRINTF    = 0x0100
    /** putchar(c): int */
    const val PUTCHAR   = 0x0101
    /** puts(s): int — writes string + newline */
    const val PUTS      = 0x0102
    /** getchar(): int */
    const val GETCHAR   = 0x0103
    /** sprintf(buf, fmt, ...): int — buf deepest, fmt on top, varargs in between */
    const val SPRINTF   = 0x0104
    /** scanf(fmt, ptr...): int */
    const val SCANF     = 0x0105
    /** snprintf(buf, n, fmt, ...): int */
    const val SNPRINTF  = 0x0106

    // ── stdlib.h ─────────────────────────────────────────────────────────────
    /** malloc(size): ptr */
    const val MALLOC    = 0x0200
    /** free(ptr): void */
    const val FREE      = 0x0201
    /** exit(code): void */
    const val EXIT      = 0x0202
    /** atoi(s): int */
    const val ATOI      = 0x0203
    /** abs(n): int */
    const val ABS       = 0x0204
    /** rand(): int */
    const val RAND      = 0x0205
    /** srand(seed): void */
    const val SRAND     = 0x0206
    /** calloc(nmemb, size): ptr */
    const val CALLOC    = 0x0207

    // ── string.h ─────────────────────────────────────────────────────────────
    /** strlen(s): int */
    const val STRLEN    = 0x0300
    /** strcpy(dst, src): ptr (dst) */
    const val STRCPY    = 0x0301
    /** strncpy(dst, src, n): ptr (dst) */
    const val STRNCPY   = 0x0302
    /** strcat(dst, src): ptr (dst) */
    const val STRCAT    = 0x0303
    /** strcmp(s1, s2): int */
    const val STRCMP    = 0x0304
    /** strncmp(s1, s2, n): int */
    const val STRNCMP   = 0x0305
    /** strchr(s, c): ptr or 0 */
    const val STRCHR    = 0x0306
    /** strstr(haystack, needle): ptr or 0 */
    const val STRSTR    = 0x0307
    /** memcpy(dst, src, n): ptr (dst) */
    const val MEMCPY    = 0x0308
    /** memmove(dst, src, n): ptr (dst) */
    const val MEMMOVE   = 0x0309
    /** memset(ptr, c, n): ptr */
    const val MEMSET    = 0x030A
    /** memcmp(s1, s2, n): int */
    const val MEMCMP    = 0x030B
    /** strdup(s): ptr — allocates new string */
    const val STRDUP    = 0x030C

    // ── windows.h ────────────────────────────────────────────────────────────
    /** GetLastError(): DWORD */
    const val GET_LAST_ERROR    = 0x0400
    /** SetLastError(code): void */
    const val SET_LAST_ERROR    = 0x0401
    /** Sleep(ms): void */
    const val SLEEP             = 0x0402
    /** GetTickCount(): DWORD — milliseconds since VM start */
    const val GET_TICK_COUNT    = 0x0403
    /** ExitProcess(code): void */
    const val EXIT_PROCESS      = 0x0404
    /** GetSystemTime(pSYSTEMTIME): void — writes 8 WORDs to struct */
    const val GET_SYSTEM_TIME   = 0x0405
    /** QueryPerformanceCounter(lpPerformanceCount): BOOL */
    const val QUERY_PERFORMANCE_COUNTER = 0x0406
    /** QueryPerformanceFrequency(lpFrequency): BOOL */
    const val QUERY_PERFORMANCE_FREQUENCY = 0x0407
    /** OutputDebugStringA(lpOutputString): void */
    const val OUTPUT_DEBUG_STRING = 0x0408
}
