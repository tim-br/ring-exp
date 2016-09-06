(ns clj-tpl-chess.htmp-tmpl)

(def signin-html
  (str "<html>
       <form action='user' method='post'>
       User Name:<br>
       <input type='text' name='user-name'><br>
       Password:<br>  <input type='password' name='password'>
       <br>
       <input type='submit' value=\"Submit\">
       </form>
  </html>"))