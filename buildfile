require 'buildr/single_intermediate_layout'
require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/custom_pom'

desc 'GWT AppCache Support Library'
define 'gwt-appcache' do
  project.group = 'org.realityforge.gwt.appcache'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('realityforge/gwt-appcache')
  pom.add_developer('realityforge', 'Peter Donald')
  pom.add_developer('dankurka', 'Daniel Kurka')
  pom.provided_dependencies.concat [:javax_servlet, :javax_annotation, :gwt_user, :gwt_dev]

  desc 'GWT AppCache client code'
  define 'client' do
    compile.with :javax_annotation, :gwt_user

    test.using :testng
    test.with :mockito

    package(:jar).include("#{_(:source, :main, :java)}/*")
    package(:sources)
    package(:javadoc)
  end

  desc 'GWT AppCache Linker'
  define 'linker' do
    compile.with :javax_annotation, :gwt_user, :gwt_dev, project('server')

    test.using :testng
    test.with :mockito

    package(:jar)
    package(:sources)
    package(:javadoc)
  end

  desc 'GWT AppCache server code'
  define 'server' do
    compile.with :javax_servlet, :javax_annotation, :gwt_user, :gwt_dev

    test.using :testng
    test.with :mockito

    package(:jar)
    package(:sources)
    package(:javadoc)
  end
end
