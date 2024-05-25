package com.store.controllers
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class Product(
    val id: Int,
    @field:NotBlank(message = "Name is mandatory")
    val name: String,
    @field:NotBlank(message = "Type is mandatory")
    val type: String,
    @field:NotNull(message = "Cost is mandatory")
    @field:Min(value = 0, message = "Cost must be a positive number")
    val cost: Float,
    @field:NotNull(message = "Inventory is mandatory")
    @field:Min(value = 0, message = "Inventory must be a non-negative number")
    val inventory: Int
)

data class ErrorResponseBody(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val path: String
)

data class ProductId(
    val id: Int
)

@RestController
@RequestMapping("/products")
class ProductsController {

    private val products = ConcurrentHashMap<Int, Product>()
    private var currentId = 1

    @PostMapping
    fun addProduct(@Valid @RequestBody product: Product): ResponseEntity<Any> {
        val newProduct = product.copy(id = currentId++)
        products[newProduct.id!!] = newProduct
        return ResponseEntity(ProductId(newProduct.id), HttpStatus.CREATED)
    }

    @GetMapping
    fun getProducts(@RequestParam(required = false) type: String?): ResponseEntity<Any> {
        val filteredProducts = type?.let {
            products.values.filter { it.type == type }
        } ?: products.values.toList()

        return ResponseEntity(filteredProducts, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Int): ResponseEntity<Any> {
        val product = products[id] ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(product, HttpStatus.OK)
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: Int, @Valid @RequestBody updatedProduct: Product): ResponseEntity<Any> {
        if (!products.containsKey(id)) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        products[id] = updatedProduct.copy(id = id)
        return ResponseEntity(updatedProduct, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Int): ResponseEntity<Any> {
        val existingProduct = products.remove(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: Exception): ResponseEntity<ErrorResponseBody> {
        val errorResponse = ErrorResponseBody(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = ex.localizedMessage,
            path = "/products"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
}
