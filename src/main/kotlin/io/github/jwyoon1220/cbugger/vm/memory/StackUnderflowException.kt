package io.github.jwyoon1220.cbugger.vm.memory

class StackUnderflowException : RuntimeException("Stack underflow: attempted to pop from an empty stack")
