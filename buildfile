require 'buildr/single_intermediate_layout'
require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'

desc 'GWT Appcache Linker and server support'
define 'gwt-appcache' do
  project.group = 'org.realityforge.gwt.appcache'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  define 'client' do
    compile.with :gwt_user, :gwt_dev, project('client')

    test.using :testng
    test.with :mockito

    package(:jar).include("#{_(:source, :main, :java)}/*")
    package(:sources)
  end

  define 'linker' do
    compile.with :gwt_user, :gwt_dev, project('server')

    test.using :testng
    test.with :mockito

    package(:jar)
    package(:sources)
  end

  define 'server' do
    compile.with :javax_servlet, :gwt_user, :gwt_dev

    test.using :testng
    test.with :mockito

    package(:jar)
    package(:sources)
  end
end
