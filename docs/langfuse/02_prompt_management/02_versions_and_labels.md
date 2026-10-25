# Versions and labels

## Versions

Every change of a prompt creates a new version. The versions have the numbers 1, 2, 3 and so on.
A version is immutable.

## Labels

A label is a pointer to one version of a prompt.
One version can have many labels. A label points to one version of a prompt at a time.

| Label        | Meaning                                                        |
|--------------|----------------------------------------------------------------|
| `production` | The default label. The API returns this version without parameters. |
| `latest`     | Langfuse sets it automatically on the newest version.          |
| custom       | For example `staging`, a tenant name or a name of an experiment. |

## Resolving a label

| Request                      | Result                                                    |
|------------------------------|-----------------------------------------------------------|
| no `label`, no `version`     | The version with the label `production`.                  |
| `label=staging`              | The version with the label `staging`.                     |
| `version=3`                  | Version 3, regardless of its labels.                      |
| `label` and `version` together | The error `400`.                                        |

When no version has the requested label, the API returns `404`. There is no fallback to `production` or `latest`.

## Deployment and rollback

1. A new version gets the label `latest`.
2. Test the version, for example in the Playground.
3. Assign the label `production` to the version. This deploys it.
4. To roll back, assign the label `production` to the previous version in the UI.

The code of the application stays unchanged during these steps.
