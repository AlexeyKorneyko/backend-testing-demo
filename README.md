## Comparison of conceptual differences in testing provided by kotlin backend frameworks

### App description
The app consists of an endpoint that returns the average price of a product from a marketplace.

```http request
GET /product/$id

{   
    “id”: “bicycle”,
    “averagePrice”: 250.29
}
```

The only tricky part is that prices are sourced from external party via http.
