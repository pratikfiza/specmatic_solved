package com.store.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

data class Product(
    val id: String,
    val name: String,
    val type: String,
    val cost: Float
)

@RestController
@RequestMapping("/products")
class ProductsController {

    private val products = ConcurrentHashMap<String, Product>()

    @PostMapping
    fun addProduct(@RequestBody product: Product?): ResponseEntity<Any> {
        if (product == null || product.id.isBlank() || product.name.isBlank() || product.type.isBlank() || product.cost <= 0) {
            return ResponseEntity("Invalid product data", HttpStatus.BAD_REQUEST)
        }

        products[product.id] = product
        return ResponseEntity(product, HttpStatus.CREATED)
    }

    @GetMapping
    fun getProducts(): ResponseEntity<List<Product>> {
        return ResponseEntity(products.values.toList(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: String): ResponseEntity<Product> {
        val product = products[id] ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(product, HttpStatus.OK)
    }

    @GetMapping(params = ["type"])
    fun getProductsByType(@RequestParam type: String?): ResponseEntity<Any> {
        if (type.isNullOrBlank()) {
            return ResponseEntity("Type parameter is missing or invalid", HttpStatus.BAD_REQUEST)
        }

        val filteredProducts = products.values.filter { it.type == type }
        return ResponseEntity(filteredProducts, HttpStatus.OK)
    }
}
