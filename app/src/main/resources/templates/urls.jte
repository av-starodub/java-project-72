@import hexlet.code.page.UrlsPage
@param UrlsPage page

@template.base(
page = page,
content = @`
    <section>
        <div class="container-lg mt-5">
            <h1>Сайты</h1>
            <table class="table table-bordered table-hover mt-3">
                <thead>
                <tr>
                    <th class="col-1">ID</th>
                    <th>Имя</th>
                    <th class="col-2">Последняя проверка</th>
                    <th class="col-1">Код ответа</th>
                </tr>
                </thead>
                <tbody>
                @for(var entry : page.getUrlToLastCheckMap().entrySet())
                    !{var url = entry.getKey();}
                    !{var urlCheck = entry.getValue();}
                    <tr>
                        <td>
                            ${url.getId()}
                        </td>
                        <td>
                            <a href="/urls/${url.getId()}">${url.getName()}</a>
                        </td>
                        <td>
                            ${page.formatTimestamp(urlCheck.getCreatedAt(), "dd/MM/yyyy HH:mm")}
                        </td>
                        <td>
                            ${urlCheck.getStatusCode()}
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </section>
`
)
