module RealityForge
  class Pom
    Developer = Struct.new(:id, :name, :email)

    attr_writer :name

    def name
      @name || @buildr_project.comment || @buildr_project.name
    end

    attr_writer :description

    def description
      @description || name
    end

    attr_accessor :url

    def licenses
      @licenses ||= {}
    end

    def add_apache2_license
      self.licenses["The Apache Software License, Version 2.0"] = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    end

    attr_accessor :scm_url
    attr_accessor :scm_connection
    attr_accessor :scm_developer_connection

    # Add a project like add_github_project("realityforge/gwt-appcache")
    def add_github_project(project_spec)
      git_url = "git@github.com:#{project_spec}.git"
      self.scm_connection = self.scm_developer_connection = "scm:git:#{git_url}"
      self.scm_url = git_url
    end

    def developers
      @developers ||= []
    end

    def add_developer(id, name = nil, email = nil)
      self.developers << Developer.new(id, name, email)
    end

    def provided
      @provided ||= []
    end

    def runtime
      @runtime ||= []
    end

    protected

    def associate_project(buildr_project)
      @buildr_project = buildr_project
    end

    # :call-seq:
    #   pom_xml => string
    #
    def self.pom_xml(project, package)
      Proc.new do
        xml = Builder::XmlMarkup.new(:indent => 2)
        xml.instruct!
        xml.project do
          xml.modelVersion '4.0.0'
          xml.parent do
            xml.groupId "org.sonatype.oss"
            xml.artifactId "oss-parent"
            xml.version "7"
          end
          xml.groupId project.group
          xml.artifactId project.id
          xml.version project.version
          xml.packaging package.type.to_s
          xml.classifier package.classifier if package.classifier

          xml.name project.pom.name if project.pom.name
          xml.description project.pom.description if project.pom.description
          xml.url project.pom.url if project.pom.url

          xml.licenses do
            project.pom.licenses.each_pair do |name, url|
              xml.license do
                xml.name name
                xml.url url
                xml.distribution 'repo'
              end
            end
          end

          if project.pom.scm_url || project.pom.scm_connection || project.pom.scm_developer_connection
            xml.scm do
              xml.connection project.pom.scm_connection if project.pom.scm_connection
              xml.developerConnection project.pom.scm_developer_connection if project.pom.scm_developer_connection
              xml.url project.pom.scm_url if project.pom.scm_url
            end
          end

          xml.developers do
            project.pom.developers.each do |developer|
              xml.developer do
                xml.id developer.id
                xml.name developer.name if developer.name
                xml.email developer.email if developer.email
              end
            end
          end

          xml.dependencies do
            provided_deps = Buildr.artifacts(project.pom.provided).collect{|d| d.to_s}
            runtime_deps = Buildr.artifacts(project.pom.runtime).collect{|d| d.to_s}
            deps =
              Buildr.artifacts(project.compile.dependencies).
                select{|d| d.is_a?(Artifact)}.
                collect do |d|
                f = d.to_s
                scope = provided_deps.include?(f) ? "provided" :
                  runtime_deps.include?(f) ? "runtime" :
                  "compile"
                d.to_hash.merge(:scope => scope)
              end
            deps.each do |dependency|
              xml.dependency do
                xml.groupId dependency[:group]
                xml.artifactId dependency[:id]
                xml.version dependency[:version]
                xml.scope dependency[:scope] unless dependency[:scope] == "compile"
              end
            end
          end
        end
      end
    end
  end
end

module Buildr
  class Project #:nodoc:
    def pom
      unless @pom
        @pom = parent ? parent.pom.dup : RealityForge::Pom.new
        @pom.send :associate_project, self
      end
      @pom
    end
  end
end

module Buildr
  module Package
    alias :old_package :package

    def package(*args)
      package = old_package(*args)
      class << package
        def pom
          unless @pom || classifier
            pom_filename = Util.replace_extension(name, 'pom')
            spec = {:group => group, :id => id, :version => version, :type => :pom}
            @pom = Buildr.artifact(spec, pom_filename)
            buildr_project =  Buildr.project(self.scope.join(':'))
            @pom.content RealityForge::Pom.pom_xml(buildr_project, self)
          end
          @pom
        end
      end
      package.instance_variable_set("@pom", nil)
      package.enhance([package.pom.to_s]) if package.type.to_s == 'jar' && !package.classifier
      package
    end
  end

  module ActsAsArtifact
    def pom_xml
      self.pom.content
    end
  end
end

