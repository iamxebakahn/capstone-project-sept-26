# ShopNest catalog service
- Run unit tests with: mvn -q test -Dtest='ProductMapperTest,ArrayUtilTests'
- Never run plain `mvn test`: the context test needs MySQL.
- Controller -> IProductService -> repository. DTOs only in and out of controllers.
- Constructor injection only.

- Library or cloud docs: ask the Context7 tool first, name the page you used, and check any artifact on Maven Central.
- The catalog MCP server is read-only. Do not add write tools or tokens to it without asking.
- Never paste a secret into a prompt or a note.
