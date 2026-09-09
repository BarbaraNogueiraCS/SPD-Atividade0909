"""Executa o notebook com IJava e salva somente após todas as células passarem."""
from pathlib import Path

import nbformat
from nbclient import NotebookClient

raiz = Path(__file__).resolve().parents[1]
arquivo = raiz / "notebooks" / "testes_persistencia.ipynb"
notebook = nbformat.read(arquivo, as_version=4)
NotebookClient(
    notebook,
    timeout=120,
    kernel_name="java",
    resources={"metadata": {"path": str(arquivo.parent)}},
).execute()
nbformat.write(notebook, arquivo)
print("Notebook executado com sucesso; saídas salvas em", arquivo)
