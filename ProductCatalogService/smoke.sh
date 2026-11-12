#!/usr/bin/env bash
email="smoke${RANDOM}@shopnest.com"
auth_body="{\"email\":\"$email\",\"password\":\"Pass@12345\"}"
product='{"name":"Smoke Candle","description":"Test item","price":199.00,"imageUrl":"candle.com","categoryDto":{"name":"HOME","description":"Home items"}}'
fail=0
check() {
  [ "$2" = "$3" ] && printf 'PASS %s expected %s got %s\n' "$1" "$2" "$3" || { printf 'FAIL %s expected %s got %s\n' "$1" "$2" "$3"; fail=1; }
}
c=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$AUTH_URL/auth/signup" -H 'Content-Type: application/json' -d "$auth_body")
check 1 201 "$c"
r=$(curl -s -w '\n%{http_code}' -X POST "$AUTH_URL/auth/login" -H 'Content-Type: application/json' -d "$auth_body")
c=$(printf '%s\n' "$r" | sed -n '$p')
b=$(printf '%s\n' "$r" | sed '$d')
token=$(printf '%s\n' "$b" | sed -n 's/.*"token":[[:space:]]*"\([^"]*\)".*/\1/p')
if [ "$c" = 200 ] && [ -n "$token" ]; then
  printf 'PASS 2 expected 200 got %s\n' "$c"
else
  printf 'FAIL 2 expected 200 got %s\n' "$c"
  fail=1
fi
c=$(curl -s -o /dev/null -w '%{http_code}' "$CATALOG_URL/products/999999")
check 3 404 "$c"
c=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$CATALOG_URL/products" -H 'Content-Type: application/json' -d "$product")
check 4 401 "$c"
r=$(curl -s -w '\n%{http_code}' -X POST "$CATALOG_URL/products" -H 'Content-Type: application/json' -H "Authorization: Bearer $token" -d "$product")
c=$(printf '%s\n' "$r" | sed -n '$p')
b=$(printf '%s\n' "$r" | sed '$d')
id=$(printf '%s\n' "$b" | sed -n 's/.*"id":[[:space:]]*\([0-9][0-9]*\).*/\1/p')
if [ "$c" = 201 ] && [ -n "$id" ]; then
  printf 'PASS 5 expected 201 got %s\n' "$c"
else
  printf 'FAIL 5 expected 201 got %s\n' "$c"
  fail=1
fi
if [ -z "$id" ]; then
  printf 'SKIP 6\n'
  printf 'SKIP 7\n'
  exit 1
fi
c=$(curl -s -o /dev/null -w '%{http_code}' "$CATALOG_URL/products/$id")
check 6 200 "$c"
c=$(curl -s -o /dev/null -w '%{http_code}' -X DELETE "$CATALOG_URL/products/$id" -H "Authorization: Bearer $token")
check 7 403 "$c"
exit "$fail"
