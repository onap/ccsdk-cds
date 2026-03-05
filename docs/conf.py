project = "onap"
release = "master"
version = "master"

author = "Open Network Automation Platform"
# yamllint disable-line rule:line-length
copyright = "ONAP. Licensed under Creative Commons Attribution 4.0 International License"

pygments_style = "sphinx"
html_theme = "sphinx_rtd_theme"
html_theme_options = {
  "style_nav_header_background": "white",
  "sticky_navigation": "False" }
html_logo = "_static/logo_onap_2024.png"
html_favicon = "_static/favicon.ico"
html_static_path = ["_static"]
html_show_sphinx = False

extensions = [
    'sphinx.ext.intersphinx',
    'sphinx.ext.graphviz',
    'sphinxcontrib.blockdiag',
    'sphinxcontrib.seqdiag',
    'sphinxcontrib.plantuml',
    'sphinx_tabs.tabs'
]

#
# Map to 'latest' if this file is used in 'latest' (master) 'doc' branch.
# Change to {releasename} after you have created the new 'doc' branch.
#

branch = 'latest'

intersphinx_mapping = {}
doc_url = 'https://docs.onap.org/projects'
master_doc = 'index'

exclude_patterns = ['.tox']

spelling_word_list_filename='spelling_wordlist.txt'
spelling_lang = "en_GB"

intersphinx_mapping['onap-integration']    = ('{}/onap-integration/en/%s'.format(doc_url) % branch, None)

html_last_updated_fmt = '%d-%b-%y %H:%M'

def setup(app):
    app.add_css_file("css/ribbon.css")

linkcheck_ignore = [
  # Local/development URLs referenced in installation and usage guides
  r'http://localhost:\d+/',
  r'https?://127\.0\.0\.1:\d+/',
  # GitHub rate-limits CI runners (HTTP 429) making checks unreliable;
  # also, line-number anchors (#L123) are rendered via JavaScript and
  # cannot be resolved by the link checker
  r'https://github\.com/onap/',
  # Wiki attachment/thumbnail images were lost during the wiki.onap.org
  # migration to lf-onap.atlassian.net; the old download URLs now 404
  r'https://wiki\.onap\.org/download/attachments/',
  r'https://wiki\.onap\.org/download/thumbnails/',
]
