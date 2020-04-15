<#import "lib/utils.ftl" as u>

<@u.page>
    <form action="/login/" method="post">
        Username: <input type="text" name="username" value="bart"><br>
        Password: <input type="password" name="password" value="1234"><br>
        <input type="submit">
    </form>
</@u.page>
