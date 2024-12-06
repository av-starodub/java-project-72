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
                @for(var urlDto : page.getUrlDtos())
                    <tr>
                        <td>
                            ${urlDto.getId()}
                        </td>
                        <td>
                            <a href="/urls/${urlDto.getId()}">${urlDto.getName()}</a>
                        </td>
                        <td>
                            ${page.formatTimestamp(urlDto.getLatestCheckCreatedAt(), "dd/MM/yyyy HH:mm")}
                        </td>
                        <td>
                            ${urlDto.getLatestCheckStatusCode()}
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </section>
`
)
