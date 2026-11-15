Le workflow de semgrep analyse tika-core et tika-serialization. Il compare le commit de l'état précédent, le pull request, avec le commit du push, le main, et échoue sur les nouvelles alertes.



Semgrep est utiliser grace dans l'image Docker avec Semgrep installé et l'utilise pour exécuté les commandes Semgrep dans GitHub Actions. https://docs.semgrep.dev/semgrep-ci/sample-ci-configs





On utilise les règles par défaut de semgrep pour détecter les vulnérabilité. De plus, pour comparer avec le commit antérieur, on doit récupérer l'historique avec fetch-depth:0 puis on vérifie que la référence désigne un commit disponivle pour éviter qu'un problème de référence soit traiter comme un commit sans erreur avec git cat-file -e. On arrête l'étape en cas d'erreur de commande, de variable non définie ou de pipeline en échec avec set-euo pipefail. On autorise aussi a utilisé le checkout du conteneur avec safe.directory puis on permet que la permission de lire pour évité que le scan modifie des fichiers.





**Les vulnérabilités**





1.Chiffrement AES en mode ECB



La méthode vulnérable toEncryptedJson dans le fichier JsonMetadata.java, car le fichier permet de transformé les objets Metadata en json. De plus, on n'a pas besoin de créer de dépendance ou de classe dans ce fichier pour introduire les vulnérabilité. 



Le type de vulnérabilité est CWE-327 qui correspond a l'utilisation d'un algorithme ou d'un mode cryptographique risqué. La méthode utilise la même clé pour produire deux blocs chiffrés identiques. On peut donc repérer des répétitions dans les données sérialisées et déduire des information sur leur structure. Ainsi le chiffrement ne fournit pas non plus de mécanisme d'authentification des données. 



La règle correspondant est java.lang.security.audit.crypto.ecb-cipher.ecb-cipher`.v Sa sévérité est WARNING. Elle recherche une affectation à une variable Cipher avec un mode contenant ECB.



2\. Désérialization sans restriction des classes



La méthode readLegacySerializeMetadata est dans le fichier JsonMetadata.java, car le fichier gère la lecture des métadonnées. Cela permet de comparer une lecture structurer dans Json à une lecture d'objet java.



Le type de vulnérabilité est CWE-502 qui correspond à a désérialisation de données non fiables. Le flux de données est désérialisé sans de filtre ni liste de classes autorisées. La méthode retourne aussi Objet et donc elle peut accepter des classes qui ne représentent pas des métadonnées, La désérialisation peut déclencher leurs callbacks readObject avant que l'appelant n'inspecte le résultat. Ainsi un flux peut provoquer des comportements indésirables si des classes exploitables sont présentés dans le classpath,



La règle correspondant est java.lang.security.audit.object-deserialization.object-deserialization. Sa sévérité est WARNING. Elle recherche la construction new ObjectInputStream(...).



3\.



4\.



5\.



6\.



7\.





**Rapport semgrep**





