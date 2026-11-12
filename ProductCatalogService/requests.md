# ShopNest smoke test plan

Use environment variables AUTH_URL and CATALOG_URL. Password for the test user: Pass@12345.
Use a new unique email on every run (smoke plus a random number, at shopnest.com).
Reading a product needs no token. Creating and deleting need a token.

| # | Request | Expect |
|---|---|---|
| 1 | POST {auth}/auth/signup with {"email":"<unique email>","password":"Pass@12345"} | 201 |
| 2 | POST {auth}/auth/login with the same body | 200 and a "token" field |
| 3 | GET {catalog}/products/999999 with NO token | 404 |
| 4 | POST {catalog}/products with NO token, product body below | 401 |
| 5 | POST {catalog}/products with the token, product body below | 201, response has the new product "id" |
| 6 | GET {catalog}/products/<id from request 5> with NO token | 200 |
| 7 | DELETE {catalog}/products/<id from request 5> with the token | 403 (a normal USER cannot delete) |

Product body: {"name":"Smoke Candle","description":"Test item","price":199.00,"imageUrl":"candle.com","categoryDto":{"name":"HOME","description":"Home items"}}