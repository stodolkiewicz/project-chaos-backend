This cert is to be used locally to seal normal kubernetes secrets.
kubeseal --cert pub-cert.pem < secret.yaml > sealed-secret.yaml