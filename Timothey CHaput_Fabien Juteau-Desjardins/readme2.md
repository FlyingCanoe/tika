Le workflow de semgrep analyse tika-core et tika-serialization. Il compare le commit de l'état précédent, le pull request, avec le commit du push, le main, et échoue sur les nouvelles alertes.



Semgrep est utiliser grace dans l'image Docker avec Semgrep installé et l'utilise pour exécuté les commandes Semgrep dans GitHub Actions. https://docs.semgrep.dev/semgrep-ci/sample-ci-configs





On utilise les règles par défaut de semgrep pour détecter les vulnérabilité. De plus, pour comparer avec le commit antérieur, on doit récupérer l'historique avec fetch-depth:0 puis on vérifie que la référence désigne un commit disponivle pour éviter qu'un problème de référence soit traiter comme un commit sans erreur avec git cat-file -e. On arrête l'étape en cas d'erreur de commande, de variable non définie ou de pipeline en échec avec set-euo pipefail. On autorise aussi a utilisé le checkout du conteneur avec safe.directory puis on permet que la permission de lire pour évité que le scan modifie des fichiers.









