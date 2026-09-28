{ pkgs, lib, config, inputs, ... }:

{
  # https://devenv.sh/languages/
  languages.java = {
    enable = true;
    jdk.package = pkgs.temurin-bin-21;
  };

  # See full reference at https://devenv.sh/reference/options/
}
