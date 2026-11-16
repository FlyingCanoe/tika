Le workflow de semgrep analyse tika-core et tika-serialization. Il compare le commit de l'état précédent, le pull request, avec le commit du push, le main, et échoue sur les nouvelles alertes.



Semgrep est utiliser grace dans l'image Docker avec Semgrep installé et l'utilise pour exécuté les commandes Semgrep dans GitHub Actions. https://docs.semgrep.dev/semgrep-ci/sample-ci-configs





On utilise les règles par défaut de semgrep pour détecter les vulnérabilité. De plus, pour comparer avec le commit antérieur, on doit récupérer l'historique avec fetch-depth:0 puis on vérifie que la référence désigne un commit disponivle pour éviter qu'un problème de référence soit traiter comme un commit sans erreur avec git cat-file -e. On arrête l'étape en cas d'erreur de commande, de variable non définie ou de pipeline en échec avec set-euo pipefail. On autorise aussi a utilisé le checkout du conteneur avec safe.directory puis on permet que la permission de lire pour évité que le scan modifie des fichiers.





**Les vulnérabilités**





1\. Chiffrement AES en mode ECB



La méthode vulnérable toEncryptedJson dans le fichier JsonMetadata.java, car le fichier permet de transformé les objets Metadata en json. De plus, on n'a pas besoin de créer de dépendance ou de classe dans ce fichier pour introduire les vulnérabilité. 



Le type de vulnérabilité est CWE-327 qui correspond a l'utilisation d'un algorithme ou d'un mode cryptographique risqué. La méthode utilise la même clé pour produire deux blocs chiffrés identiques. On peut donc repérer des répétitions dans les données sérialisées et déduire des information sur leur structure. Ainsi le chiffrement ne fournit pas non plus de mécanisme d'authentification des données. 



La règle correspondant est java.lang.security.audit.crypto.ecb-cipher.ecb-cipher`.v Sa sévérité est WARNING. Elle recherche une affectation à une variable Cipher avec un mode contenant ECB.



2\. Désérialization sans restriction des classes



La méthode readLegacySerializeMetadata est dans le fichier JsonMetadata.java, car le fichier gère la lecture des métadonnées. Cela permet de comparer une lecture structurer dans Json à une lecture d'objet java.



Le type de vulnérabilité est CWE-502 qui correspond à a désérialisation de données non fiables. Le flux de données est désérialisé sans de filtre ni liste de classes autorisées. La méthode retourne aussi Objet et donc elle peut accepter des classes qui ne représentent pas des métadonnées, La désérialisation peut déclencher leurs callbacks readObject avant que l'appelant n'inspecte le résultat. Ainsi un flux peut provoquer des comportements indésirables si des classes exploitables sont présentés dans le classpath,



La règle correspondant est java.lang.security.audit.object-deserialization.object-deserialization. Sa sévérité est WARNING. Elle recherche la construction new ObjectInputStream(...).



3\.
 Dans le fichier `tika-core/src/main/java/org/apache/tika/io/FileSource.java`, J'ai ajouté une utilisation de
 `PrivilagedAction` qui permet de potentiellement contourné une restriction imposée par un `SecurityManager`,
 semgrep considère que cela est une vulnérabilité. Cela est discutable sachant que le `SecurityManager` n'est pas
 généralement considéré comme offrant une vrait protection et va pour cette raison être retiré d'une future version du sdk.
 
 Le fichier a été choisi, car il fait de IO est a donc le potentiel d'interagir avec le `SecurityManager`.



4\.
 Toujours dans le même fichier, j'ai ajouté un function qui change les permissions d'un fichier pour donnée
 à tous les utilisateurs toutes les permissions (dans le modél posix). Cela n'est pas en soit un vulnerabilité,
 mais cela a le potentiel d'en créer si, par exemple, on l'exécuterait sur `/etc/group` ou similaire.

5\. En regardant dans la liste des vulnérabilités, j'ai qui est censé être capable des "reverse shell". les
    "reverse shell" sont du code qui ce connect a un serveur distance pour recevoir une liste de commende a éxecuté
    generalement dans le but de prendre control de l'ordinateur

Cela est fait dans le fichier `tika-core/src/main/java/org/apache/tika/parser/NetworkParser.java`. Le fichier a été choisi,
car il touche au ressaut.
    
Même si j'ai dû modifier le code pour éliminer des erreurs de compilation, semgrep a quand même été capable
de le reconnaitre.

La presence dans "reverse shell" n'ait pas typiquement indiquatif d'une vulnérabilité, mais plutôt d'un verus.

6\. 
Toujours, dans NetworkParser, un des protocol suporté semble être telnet, ce qui en soit est un problem.

Je me suit dit que cela serait un bon endroit pour insérer une tentative bâclé de récuperer de la securité
en jouent avec de la cryptographie.

J'ai donc fait du code qui encrypt le donné en utilisant RC2 avant de les avoié. Pour cela, j'utilise
une clé qui est [garantie d'être aléatoire](https://xkcd.com/221/).

L'algorithme RC2 est un très vieux algorithm d'encryption symétrique. Cette algorithm n'est plus considèrer comme
sécuritaire, ce qui est une vulnérabilité.

Pour une raison que je ne suis pas sûre de vouloir connaitre, faire ceci est brisé des tests pour
des modules à l'extérieur de tika-core.


7\.
dans `tika-core/src/main/java/org/apache/tika/io/ByteArraySource.java`, j'ai ajouté une methode qui prend
la source entière et la deserializes dans un objet arbitraire en utilisant `ObjectInputStream`.

`ObjectInputStream` est un method de deserialiation, qui comme beaucoup d'autre methode de deserialisation mal concu,
permet d'exécuter du code arbitraire quand on lui donne un exemple advereserielle, ce qui est une vulnérabilité.

# Vunerabilité non-détecté
J'ai également esaiyer d'introduire une vulnerabilité en utilisant un mot de pass hardcodé pour telnet,
bien que j'ai nommé la variable "password" et que ca valeur est un "string literal", semgrep ne la pas détecter.

Ce qui est a mes yeux un problèm majeur. Détecter des "credential" qui sont inclue dans le code source est
une des utilités principale des scanneurs des vulnérabilité qui oppaire sur le code source.

Il aussi interesent de noté que j'ai inséré un vulnérabilité équivalant pour la vunerabilité 6. la
clé est hardcodé. Dans ce cas, semgrep n'a aussi pas été capable de le détécté, il a seulment détécté
que un algorithm non sécuritaire a été utilisé.


# Modification du ci

Durrent la tache 2, ca m'agaçait que le ci prenait l'équivalant de deux heures pour s'exécuter, je l'avais
donc grandement réduit en faisait qu'il n'exécute que les tests sur une platform et pour un seul sdk.

J'ai aussi enlevé les autres action qui n'exécutait pas les tests.

Durent la tache 3, on ajouté dans une nouvelle action semgrep. spécifiquement on a fait en sort
que semgrep échut si il détect une nouvelle vunerabiliter dans tika-core. Cela a été fait car, 
de base semgrep detéctait un tas de problèble préexistant et que la tache était d'ajouter de nouveau
problèm detectable, pas de classifé/corrigé les problème existant dans tika.

Le rickroll a été ajouté au test, donc quand les tests échut (pas semgrep) le rickroll joue.
Sachant que la vulnerabilité 6 fait échouer les tests, le rickroll se trouve à s'éxecuté.




